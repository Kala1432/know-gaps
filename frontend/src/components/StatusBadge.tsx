import React from 'react';

interface StatusBadgeProps {
  type: 'prerequisite' | 'risk' | 'mastery' | 'activity';
  value: string | number;
}

export const StatusBadge: React.FC<StatusBadgeProps> = ({ type, value }) => {
  if (type === 'prerequisite') {
    const status = String(value).toUpperCase();
    if (status === 'SATISFIED') {
      return (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
          SATISFIED
        </span>
      );
    }
    if (status === 'DEFICIT') {
      return (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">
          MISSING PREREQ
        </span>
      );
    }
    return (
      <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20">
        BLOCKED
      </span>
    );
  }

  if (type === 'risk') {
    const risk = String(value).toLowerCase();
    if (risk === 'high') {
      return (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-rose-500/10 text-rose-400 border border-rose-500/20">
          HIGH RISK
        </span>
      );
    }
    if (risk === 'medium') {
      return (
        <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-amber-500/10 text-amber-400 border border-amber-500/20">
          MEDIUM RISK
        </span>
      );
    }
    return (
      <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-emerald-500/10 text-emerald-400 border border-emerald-500/20">
        LOW RISK
      </span>
    );
  }

  if (type === 'mastery') {
    const score = typeof value === 'number' ? value : parseFloat(String(value));
    const pct = Math.round(score * 100);
    const color =
      pct >= 75
        ? 'bg-emerald-500/10 text-emerald-400 border-emerald-500/20'
        : pct >= 50
        ? 'bg-amber-500/10 text-amber-400 border-amber-500/20'
        : 'bg-rose-500/10 text-rose-400 border-rose-500/20';

    return (
      <span className={`px-2.5 py-0.5 rounded-full text-xs font-semibold border ${color}`}>
        {pct}% Mastery
      </span>
    );
  }

  return (
    <span className="px-2.5 py-0.5 rounded-full text-xs font-semibold bg-indigo-500/10 text-indigo-400 border border-indigo-500/20">
      {String(value)}
    </span>
  );
};
