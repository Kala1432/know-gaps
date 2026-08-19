import React, { useEffect, useState } from 'react';
import { Lightbulb, ArrowRight } from 'lucide-react';
import { Recommendation, Student } from '../types/api';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorState } from '../components/ErrorState';

interface RecommendationPageProps {
  student: Student;
  onNavigateTab: (tab: string, conceptId?: string) => void;
}

export const RecommendationPage: React.FC<RecommendationPageProps> = ({ student, onNavigateTab }) => {
  const [recommendations, setRecommendations] = useState<Recommendation[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadRecommendations = async () => {
    setLoading(true);
    setError(null);
    try {
      const recs = await api.getNextConceptRecommendations(student.id, 5);
      setRecommendations(recs);
    } catch (err: any) {
      setError(err.message || 'Failed to generate concept recommendations.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadRecommendations();
  }, [student.id]);

  if (loading) return <LoadingSpinner message="Evaluating prerequisite DAG and cognitive evidence for optimal recommendations..." />;
  if (error) return <ErrorState message={error} onRetry={loadRecommendations} />;

  return (
    <div className="space-y-6 max-w-5xl mx-auto">
      {/* Header */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60">
        <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
          <Lightbulb className="w-5 h-5 text-indigo-400" /> Next Best Concept Recommendation Engine
        </h2>
        <p className="text-sm text-slate-400 mt-1">
          Recommendations strictly enforce prerequisite graph dependencies. The system will never recommend advanced concepts until prerequisite foundations are satisfied.
        </p>
      </div>

      {/* Recommendations List */}
      {recommendations.length === 0 ? (
        <div className="bg-slate-800/60 p-12 rounded-2xl border border-slate-700/60 text-center space-y-3">
          <p className="text-slate-400 text-sm">No concept recommendations available yet.</p>
        </div>
      ) : (
        <div className="space-y-4">
          {recommendations.map((rec) => (
            <div
              key={rec.conceptId}
              className={`p-6 rounded-2xl border transition shadow-lg ${
                rec.rank === 1
                  ? 'bg-gradient-to-r from-indigo-950/60 via-slate-800 to-slate-800 border-indigo-500/50'
                  : 'bg-slate-800/60 border-slate-700/60'
              }`}
            >
              <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-6">
                <div className="space-y-3 flex-1">
                  {/* Priority Rank & Badges */}
                  <div className="flex items-center gap-3">
                    <span className="h-7 w-7 rounded-full bg-indigo-600 text-white font-extrabold text-xs flex items-center justify-center shadow">
                      #{rec.rank}
                    </span>
                    <h3 className="text-xl font-bold text-slate-100">{rec.concept}</h3>
                    <StatusBadge type="prerequisite" value={rec.prerequisiteStatus} />
                    <StatusBadge type="mastery" value={rec.mastery} />
                  </div>

                  {/* Explainability Reason */}
                  <div className="bg-slate-900/60 p-4 rounded-xl border border-slate-800 text-sm text-slate-300 space-y-1">
                    <span className="text-xs font-bold text-indigo-400 uppercase tracking-wider block">
                      Evidence-Based Justification
                    </span>
                    <p className="leading-relaxed">{rec.reason}</p>
                  </div>
                </div>

                {/* Action Trigger */}
                <div className="shrink-0 flex flex-col items-end gap-3">
                  <div className="text-right">
                    <span className="text-xs text-slate-400 block">Priority Weight</span>
                    <span className="text-sm font-extrabold text-indigo-400">
                      {Math.round(rec.priority * 100)} / 100
                    </span>
                  </div>
                  <button
                    onClick={() => onNavigateTab('assessment', rec.conceptId)}
                    className="px-5 py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm rounded-xl transition flex items-center gap-2 shadow-lg shadow-indigo-600/20 cursor-pointer"
                  >
                    Start {rec.recommendedActivity.replace('_', ' ')} <ArrowRight className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};
