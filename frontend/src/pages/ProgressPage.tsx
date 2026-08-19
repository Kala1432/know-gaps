import React, { useEffect, useState } from 'react';
import { TrendingUp, AlertOctagon, BarChart2 } from 'lucide-react';
import { ResponsiveContainer, BarChart, Bar, XAxis, YAxis, Tooltip, CartesianGrid } from 'recharts';
import { ConceptPerformance, Attempt, Student } from '../types/api';
import { api } from '../api/client';
import { StatusBadge } from '../components/StatusBadge';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorState } from '../components/ErrorState';

interface ProgressPageProps {
  student: Student;
}

export const ProgressPage: React.FC<ProgressPageProps> = ({ student }) => {
  const [performances, setPerformances] = useState<ConceptPerformance[]>([]);
  const [attempts, setAttempts] = useState<Attempt[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const loadProgressData = async () => {
    setLoading(true);
    setError(null);
    try {
      const [perfData, attemptData] = await Promise.all([
        api.getStudentPerformance(student.id).catch(() => []),
        api.getStudentAttempts(student.id).catch(() => []),
      ]);
      setPerformances(perfData);
      setAttempts(attemptData);
    } catch (err: any) {
      setError(err.message || 'Failed to load progress metrics.');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadProgressData();
  }, [student.id]);

  if (loading) return <LoadingSpinner message="Aggregating student progress trends..." />;
  if (error) return <ErrorState message={error} onRetry={loadProgressData} />;

  const chartData = performances.map((p) => ({
    name: p.conceptName.length > 14 ? `${p.conceptName.slice(0, 12)}...` : p.conceptName,
    Mastery: Math.round(p.masteryScore * 100),
    Accuracy: Math.round(p.accuracyRate * 100),
  }));

  const diagnosedGaps = performances.filter((p) => p.activeGapTypes && p.activeGapTypes.length > 0);

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <TrendingUp className="w-5 h-5 text-indigo-400" /> Cognitive Mastery & Progress Analytics
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Historical mastery progression, accuracy rates, and diagnosed knowledge vulnerabilities.
          </p>
        </div>
      </div>

      {/* Recharts Mastery & Accuracy Chart */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 space-y-4">
        <h3 className="text-base font-bold text-slate-200 flex items-center gap-2">
          <BarChart2 className="w-4 h-4 text-indigo-400" /> Concept Mastery vs Accuracy Rate
        </h3>

        {chartData.length === 0 ? (
          <p className="text-sm text-slate-400 py-8 text-center">No concept performance data available to chart.</p>
        ) : (
          <div className="h-72 w-full pt-4">
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 10, right: 20, left: -10, bottom: 0 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" />
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} />
                <YAxis stroke="#94a3b8" fontSize={12} unit="%" domain={[0, 100]} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#0f172a', borderColor: '#334155', borderRadius: '0.75rem', color: '#f8fafc' }}
                />
                <Bar dataKey="Mastery" fill="#6366f1" radius={[4, 4, 0, 0]} />
                <Bar dataKey="Accuracy" fill="#10b981" radius={[4, 4, 0, 0]} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        )}
      </div>

      {/* Diagnosed Knowledge Gaps */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 space-y-4">
        <h3 className="text-base font-bold text-slate-200 flex items-center gap-2">
          <AlertOctagon className="w-4 h-4 text-amber-400" /> Diagnosed Knowledge Vulnerabilities
        </h3>

        {diagnosedGaps.length === 0 ? (
          <p className="text-sm text-slate-400 py-4">No active persistent misconceptions or fluency lags diagnosed!</p>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {diagnosedGaps.map((g) => (
              <div key={g.conceptId} className="p-4 bg-slate-900/60 rounded-xl border border-slate-800 space-y-2">
                <div className="flex items-center justify-between">
                  <span className="font-semibold text-sm text-slate-200">{g.conceptName}</span>
                  <StatusBadge type="mastery" value={g.masteryScore} />
                </div>
                <div className="flex flex-wrap gap-1.5 pt-1">
                  {g.activeGapTypes.map((gap) => (
                    <span key={gap} className="px-2 py-0.5 rounded text-[11px] font-bold bg-amber-500/10 text-amber-400 border border-amber-500/20">
                      {gap}
                    </span>
                  ))}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Attempt Log */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 space-y-4">
        <h3 className="text-base font-bold text-slate-200">Full Evidence Attempt Log</h3>
        {attempts.length === 0 ? (
          <p className="text-sm text-slate-400">No attempts logged yet.</p>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-xs text-slate-300">
              <thead className="bg-slate-900 text-slate-400 uppercase font-semibold">
                <tr>
                  <th className="p-3">Tested Concept</th>
                  <th className="p-3">Result</th>
                  <th className="p-3">Attempt #</th>
                  <th className="p-3">Response Time</th>
                  <th className="p-3">Difficulty</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-800">
                {attempts.map((a) => (
                  <tr key={a.id} className="hover:bg-slate-800/40">
                    <td className="p-3 font-medium text-slate-200">{a.testedConceptNames.join(', ') || 'Practice Question'}</td>
                    <td className="p-3">
                      <span className={a.isCorrect ? 'text-emerald-400 font-bold' : 'text-rose-400 font-bold'}>
                        {a.isCorrect ? 'Correct' : 'Incorrect'}
                      </span>
                    </td>
                    <td className="p-3">#{a.attemptNumber}</td>
                    <td className="p-3">{a.responseTimeMs}ms</td>
                    <td className="p-3">{Math.round(a.questionDifficulty * 100)}%</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
