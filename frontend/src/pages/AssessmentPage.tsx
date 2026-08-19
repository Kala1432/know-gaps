import React, { useEffect, useState, useRef } from 'react';
import { HelpCircle, Clock, CheckCircle2, XCircle, ArrowRight, Play, Award } from 'lucide-react';
import { Question, LearningSession, Attempt, Student } from '../types/api';
import { api } from '../api/client';
import { LoadingSpinner } from '../components/LoadingSpinner';
import { ErrorState } from '../components/ErrorState';

interface AssessmentPageProps {
  student: Student;
  targetConceptId?: string;
}

export const AssessmentPage: React.FC<AssessmentPageProps> = ({ student }) => {
  const [session, setSession] = useState<LearningSession | null>(null);
  const [questions, setQuestions] = useState<Question[]>([]);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);

  const [userAnswer, setUserAnswer] = useState('');
  const [timerMs, setTimerMs] = useState(0);

  const [lastAttemptResult, setLastAttemptResult] = useState<Attempt | null>(null);
  const [submitting, setSubmitting] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const timerRef = useRef<number | null>(null);
  const startTimeRef = useRef<number>(0);

  const handleStartSession = async () => {
    setLoading(true);
    setError(null);
    try {
      const sess = await api.startSession(student.id);
      setSession(sess);
      const qList = await api.getSessionQuestions(sess.id);
      setQuestions(qList);
      setCurrentQuestionIndex(0);
      setLastAttemptResult(null);
      if (qList.length > 0) {
        startTimer();
      }
    } catch (err: any) {
      setError(err.message || 'Failed to start practice session.');
    } finally {
      setLoading(false);
    }
  };

  const startTimer = () => {
    setTimerMs(0);
    startTimeRef.current = Date.now();
    if (timerRef.current) clearInterval(timerRef.current);
    timerRef.current = window.setInterval(() => {
      setTimerMs(Date.now() - startTimeRef.current);
    }, 100);
  };

  const stopTimer = () => {
    if (timerRef.current) clearInterval(timerRef.current);
  };

  useEffect(() => {
    return () => {
      if (timerRef.current) clearInterval(timerRef.current);
    };
  }, []);

  const handleSubmitAttempt = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!session || !userAnswer.trim() || questions.length === 0) return;

    stopTimer();
    setSubmitting(true);
    const finalResponseTime = Math.max(500, Date.now() - startTimeRef.current);
    const currentQ = questions[currentQuestionIndex];

    try {
      const result = await api.submitAttempt(
        session.id,
        student.id,
        currentQ.id,
        userAnswer.trim(),
        finalResponseTime
      );
      setLastAttemptResult(result);
    } catch (err: any) {
      setError(err.message || 'Failed to submit attempt.');
    } finally {
      setSubmitting(false);
    }
  };

  const handleNextQuestion = () => {
    setUserAnswer('');
    setLastAttemptResult(null);
    if (currentQuestionIndex + 1 < questions.length) {
      setCurrentQuestionIndex((prev) => prev + 1);
      startTimer();
    } else {
      if (session) {
        api.completeSession(session.id);
      }
      setSession(null);
    }
  };

  if (loading) return <LoadingSpinner message="Initializing active learning session..." />;
  if (error) return <ErrorState message={error} onRetry={handleStartSession} />;

  const currentQ = questions[currentQuestionIndex];

  return (
    <div className="space-y-6 max-w-4xl mx-auto">
      {/* Header */}
      <div className="bg-slate-800/60 p-6 rounded-2xl border border-slate-700/60 flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold text-slate-100 flex items-center gap-2">
            <HelpCircle className="w-5 h-5 text-indigo-400" /> Assessment & Practice Studio
          </h2>
          <p className="text-sm text-slate-400 mt-1">
            Submit answers to accumulate real-time evidence on cognitive mastery and response times.
          </p>
        </div>
        {!session && (
          <button
            onClick={handleStartSession}
            className="inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 text-white rounded-xl text-sm font-semibold shadow-lg shadow-indigo-600/30 transition cursor-pointer"
          >
            <Play className="w-4 h-4 fill-current" /> Start Practice Session
          </button>
        )}
      </div>

      {/* Session Active State */}
      {session && currentQ ? (
        <div className="bg-slate-800/60 p-8 rounded-2xl border border-slate-700/60 space-y-6">
          {/* Top Bar: Progress & Response Timer */}
          <div className="flex items-center justify-between border-b border-slate-700 pb-4">
            <div className="flex items-center gap-2">
              <span className="text-xs font-bold text-indigo-400 bg-indigo-500/10 px-2.5 py-1 rounded border border-indigo-500/20">
                Question {currentQuestionIndex + 1} of {questions.length}
              </span>
              <span className="text-xs text-slate-400 font-medium">
                Difficulty: {Math.round(currentQ.baseDifficulty * 100)}%
              </span>
            </div>
            <div className="flex items-center gap-2 font-mono text-sm font-semibold text-slate-300 bg-slate-900 px-3 py-1.5 rounded-lg border border-slate-800">
              <Clock className="w-4 h-4 text-amber-400" />
              <span>{(timerMs / 1000).toFixed(1)}s</span>
            </div>
          </div>

          {/* Question Prompt */}
          <div className="space-y-3">
            <h3 className="text-lg font-bold text-slate-100">{currentQ.content}</h3>
            {currentQ.correctAnswer && (
              <p className="text-xs text-slate-500 italic">Expected Format: Case-insensitive short response</p>
            )}
          </div>

          {/* Attempt Result Feedback */}
          {lastAttemptResult ? (
            <div
              className={`p-6 rounded-xl border space-y-4 ${
                lastAttemptResult.isCorrect
                  ? 'bg-emerald-500/10 border-emerald-500/30 text-emerald-200'
                  : 'bg-rose-500/10 border-rose-500/30 text-rose-200'
              }`}
            >
              <div className="flex items-center gap-3">
                {lastAttemptResult.isCorrect ? (
                  <CheckCircle2 className="w-6 h-6 text-emerald-400" />
                ) : (
                  <XCircle className="w-6 h-6 text-rose-400" />
                )}
                <div>
                  <h4 className="text-base font-bold">
                    {lastAttemptResult.isCorrect ? 'Correct Answer!' : 'Incorrect Answer'}
                  </h4>
                  <p className="text-xs opacity-90">
                    Response Time: {lastAttemptResult.responseTimeMs}ms • Attempt #{lastAttemptResult.attemptNumber}
                  </p>
                </div>
              </div>

              <button
                onClick={handleNextQuestion}
                className="w-full py-3 bg-indigo-600 hover:bg-indigo-500 text-white font-semibold text-sm rounded-xl transition flex items-center justify-center gap-2 cursor-pointer"
              >
                Continue to Next Question <ArrowRight className="w-4 h-4" />
              </button>
            </div>
          ) : (
            /* Answer Form */
            <form onSubmit={handleSubmitAttempt} className="space-y-4">
              <div>
                <label className="text-xs font-semibold text-slate-400 block mb-2">Your Answer</label>
                <input
                  type="text"
                  value={userAnswer}
                  onChange={(e) => setUserAnswer(e.target.value)}
                  placeholder="Type your answer here..."
                  className="w-full bg-slate-900 border border-slate-700 text-slate-100 rounded-xl p-3.5 text-sm focus:outline-none focus:border-indigo-500"
                  required
                />
              </div>

              <button
                type="submit"
                disabled={submitting || !userAnswer.trim()}
                className="w-full py-3.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white font-semibold text-sm rounded-xl transition cursor-pointer"
              >
                {submitting ? 'Evaluating Evidence...' : 'Submit Answer'}
              </button>
            </form>
          )}
        </div>
      ) : (
        /* Empty State */
        <div className="bg-slate-800/60 p-12 rounded-2xl border border-slate-700/60 text-center space-y-4">
          <Award className="w-12 h-12 text-indigo-400 mx-auto" />
          <h3 className="text-lg font-bold text-slate-100">Ready to start an assessment session?</h3>
          <p className="text-sm text-slate-400 max-w-md mx-auto">
            Click "Start Practice Session" above to solve targeted questions, measure response times, and build evidence of concept mastery.
          </p>
        </div>
      )}
    </div>
  );
};
