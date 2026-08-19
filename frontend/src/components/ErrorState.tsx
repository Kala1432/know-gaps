import React from 'react';
import { AlertTriangle, RefreshCw } from 'lucide-react';

interface ErrorStateProps {
  message?: string;
  onRetry?: () => void;
}

export const ErrorState: React.FC<ErrorStateProps> = ({ message = 'Failed to load platform data.', onRetry }) => (
  <div className="bg-rose-500/10 border border-rose-500/20 rounded-xl p-6 text-center max-w-md mx-auto my-8">
    <AlertTriangle className="w-10 h-10 text-rose-400 mx-auto mb-3" />
    <h3 className="text-base font-semibold text-rose-300 mb-1">Error Encountered</h3>
    <p className="text-sm text-rose-200/80 mb-4">{message}</p>
    {onRetry && (
      <button
        onClick={onRetry}
        className="inline-flex items-center gap-2 px-4 py-2 bg-rose-600 hover:bg-rose-500 text-white rounded-lg text-sm font-semibold transition"
      >
        <RefreshCw className="w-4 h-4" /> Retry
      </button>
    )}
  </div>
);
