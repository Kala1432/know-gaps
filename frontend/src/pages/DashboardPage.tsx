import React, { useEffect, useState } from 'react';
import { Lightbulb, ArrowRight, ShieldAlert, Award, TrendingDown, Clock, CheckCircle2 } from 'lucide-react';
import { Student, ConceptPerformance, Recommendation, Attempt } from '../types/api';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorState } from '../components/ErrorState';

interface DashboardPageProps {
  student: Student;
  onNavigateTab: (tab: string, conceptId?: string) => void;
}

export const DashboardPage: React.FC<DashboardPageProps> = ({ student, onNavigateTab }) => {
  const [performances, setPerformances] = useState<ConceptPerformance[]>([]);
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [attempts, setAttempts] = useState<Attempt[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadDashboardData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [perfData, recData, attemptData] = await Promise.all([
        api.getStudentPerformance(student.id).catch(() => []),
        api.getNextConceptRecommendations(student.id, 3).catch(() => []),
        api.getStudentAttempts(student.id).catch(() => []),
      ]);
      setPerformances(perfData);
      setRecommendations(recData);
      setAttempts(attemptData);
    } catch (err: any) {
      setError(err.message || 'Failed to load dashboard.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadDashboardData();
  }, [student.id]);

  if (loading) return <LoadingSpinner message="Evaluating student cognitive evidence..." />;
  if (error) return <ErrorState message={error} onRetry={loadDashboardData} />;

  const topRecommendation = recommendations[0];
  const strongest = [...performances].sort((a, b) => b.masteryScore - a.masteryScore).slice(0, 3);
  const weakest = [...performances].sort((a, b) => a.masteryScore - b.masteryScore).slice(0, 3);
  const highRiskConcepts = performances.filter((p) => (1.0 - p.masteryScore) >= 0.50);

  return (
    <div className="space-y-6">
      {/* Welcome Banner */}
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4 bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60">
        <div>
          <h2 className="text-xl font-bold text-slate-100">Welcome back, {student.name}!</h2>
          <p className="text-sm text-slate-400 mt-1">
            Evidence-based learning status evaluated across {performances.length} tracked concept(s).
          </p>
        </div>
        <button
          onClick={() => onNavigateTab('assessment')}
          className="inline-flex items-center justify-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-sm font-semibold shadow-lg shadow-indigo-600/20 transition cursor-pointer"
        >
          Start Practice Session <ArrowRight className="w-4 h-4" />
        </button>
      </div>

      {/* CORE UX SPOTLIGHT: "What should I study next and why?" */}
      {topRecommendation && (
        <div className="bg-gradient-to-r from-indigo-900/50 via-slate-800 to-slate-800 p-6 rounded-2xl border border-indigo-500/40 shadow-xl relative overflow-hidden">
          <div className="flex items-center gap-2 text-indigo-400 font-semibold text-xs uppercase tracking-wider mb-2">
            <Lightbulb className="w-4 h-4" /> Recommended Next Concept
          </div>
          <div className="flex flex-col lg:flex-row lg:items-center lg:justify-between gap-6">
            <div className="space-y-2">
              <div className="flex items-center gap-3">
                <h3 className="text-2xl font-extrabold text-white">{topRecommendation.concept}</h3>
                <StatusBadge type="prerequisite" value={topRecommendation.prerequisiteStatus} />
              </div>
              <p className="text-slate-300 text-sm leading-relaxed max-w-2xl">
                <strong className="text-indigo-300 font-medium">Why study this next?</strong> {topRecommendation.reason}
              </p>
            </div>
            <div className="flex items-center gap-4 shrink-0">
              <div className="bg-slate-900/80 px-4 py-3 rounded-xl border border-slate-700 text-center">
                <span className="text-xs text-slate-400 block">Mastery Score</span>
                <span className="text-lg font-bold text-indigo-400">
                  {Math.round(topRecommendation.mastery * 100)}%
                </span>
              </div>
              <button
                onClick={() => onNavigateTab('assessment', topRecommendation.conceptId)}
                className="px-6 py-3.5 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm rounded-xl shadow-lg shadow-indigo-600/30 transition cursor-pointer"
              >
                Study Concept Now
              </button>
            </div>
          </div>
        </div>
      )}

      {/* Grid Summary Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {/* Strongest Concepts */}
        <div className="bg-slate-800/60 p-5 rounded-2xl border border-slate-700/60 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
              <Award className="w-4 h-4 text-emerald-400" /> Strongest Concepts
            </h3>
          </div>
          {strongest.length === 0 ? (
            <p className="text-xs text-slate-400">No mastery data yet.</p>
          ) : (
            <ul className="space-y-2.5">
              {strongest.map((c) => (
                <li key={c.conceptId} className="flex items-center justify-between text-xs bg-slate-900/50 p-2.5 rounded-lg">
                  <span className="font-medium text-slate-200">{c.conceptName}</span>
                  <StatusBadge type="mastery" value={c.masteryScore} />
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Weakest Concepts */}
        <div className="bg-slate-800/60 p-5 rounded-2xl border border-slate-700/60 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
              <TrendingDown className="w-4 h-4 text-rose-400" /> Weakest Concepts
            </h3>
          </div>
          {weakest.length === 0 ? (
            <p className="text-xs text-slate-400">No mastery data yet.</p>
          ) : (
            <ul className="space-y-2.5">
              {weakest.map((c) => (
                <li key={c.conceptId} className="flex items-center justify-between text-xs bg-slate-900/50 p-2.5 rounded-lg">
                  <span className="font-medium text-slate-200">{c.conceptName}</span>
                  <StatusBadge type="mastery" value={c.masteryScore} />
                </li>
              ))}
            </ul>
          )}
        </div>

        {/* Retention Risk Highlights */}
        <div className="bg-slate-800/60 p-5 rounded-2xl border border-slate-700/60 space-y-4">
          <div className="flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-200 flex items-center gap-2">
              <ShieldAlert className="w-4 h-4 text-amber-400" /> Retention Risks
            </h3>
            <span className="text-xs font-bold text-amber-400 bg-amber-500/10 px-2 py-0.5 rounded">
              {highRiskConcepts.length} At Risk
            </span>
          </div>
          {highRiskConcepts.length === 0 ? (
            <p className="text-xs text-slate-400">No elevated retention risks detected.</p>
          ) : (
            <ul className="space-y-2.5">
              {highRiskConcepts.slice(0, 3).map((c) => (
                <li key={c.conceptId} className="flex items-center justify-between text-xs bg-slate-900/50 p-2.5 rounded-lg">
                  <span className="font-medium text-slate-200">{c.conceptName}</span>
                  <StatusBadge type="risk" value="high" />
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      {/* Recent Progress Timeline */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 space-y-4">
        <h3 className="text-base font-bold text-slate-200 flex items-center gap-2">
          <Clock className="w-4 h-4 text-indigo-400" /> Recent Learning Progress
        </h3>
        {attempts.length === 0 ? (
          <p className="text-sm text-slate-400 py-4">No recent attempts recorded. Start an assessment session to accumulate evidence!</p>
        ) : (
          <div className="space-y-3">
            {attempts.slice(0, 5).map((a) => (
              <div key={a.id} className="flex items-center justify-between p-3.5 bg-slate-900/60 rounded-xl border border-slate-800 text-xs">
                <div className="flex items-center gap-3">
                  {a.isCorrect ? (
                    <CheckCircle2 className="w-4 h-4 text-emerald-400 shrink-0" />
                  ) : (
                    <div className="w-4 h-4 rounded-full bg-rose-500/20 border border-rose-500 text-rose-400 flex items-center justify-center font-bold text-[10px]">✕</div>
                  )}
                  <div>
                    <span className="font-semibold text-slate-200 block">{a.testedConceptNames.join(', ') || 'Practice Question'}</span>
                    <span className="text-slate-400 text-[11px]">Attempt #{a.attemptNumber} • Response Time: {a.responseTimeMs}ms</span>
                  </div>
                </div>
                <span className={`font-semibold ${a.isCorrect ? 'text-emerald-400' : 'text-rose-400'}`}>
                  {a.isCorrect ? 'Correct' : 'Incorrect'}
                </span>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
