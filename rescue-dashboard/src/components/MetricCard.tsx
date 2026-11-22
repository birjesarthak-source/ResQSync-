import React from 'react';
import { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon: LucideIcon;
  variant?: 'cyan' | 'red' | 'green' | 'amber' | 'default';
  trend?: {
    label: string;
    positive?: boolean;
  };
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  icon: Icon,
  variant = 'default',
  trend
}) => {
  const getVariantStyles = () => {
    switch (variant) {
      case 'red':
        return {
          border: 'border-red-500/30 hover:border-red-500/60',
          iconBg: 'bg-red-500/15 text-red-400',
          valueColor: 'text-red-400'
        };
      case 'cyan':
        return {
          border: 'border-cyan/30 hover:border-cyan/60',
          iconBg: 'bg-cyan/15 text-cyan',
          valueColor: 'text-cyan'
        };
      case 'green':
        return {
          border: 'border-emerald-500/30 hover:border-emerald-500/60',
          iconBg: 'bg-emerald-500/15 text-emerald-400',
          valueColor: 'text-emerald-400'
        };
      case 'amber':
        return {
          border: 'border-amber-500/30 hover:border-amber-500/60',
          iconBg: 'bg-amber-500/15 text-amber-400',
          valueColor: 'text-amber-400'
        };
      case 'default':
      default:
        return {
          border: 'border-navy-700 hover:border-navy-600',
          iconBg: 'bg-navy-700/50 text-slate-300',
          valueColor: 'text-white'
        };
    }
  };

  const styles = getVariantStyles();

  return (
    <div
      className={`bg-navy-900 rounded-xl p-5 border ${styles.border} shadow-lg shadow-black/20 transition-all hover:translate-y-[-2px]`}
    >
      <div className="flex items-start justify-between">
        <div>
          <p className="text-xs font-semibold tracking-wider uppercase text-slate-400 mb-1">{title}</p>
          <h3 className={`text-2xl font-bold font-mono ${styles.valueColor}`}>{value}</h3>
        </div>
        <div className={`p-2.5 rounded-lg ${styles.iconBg}`}>
          <Icon className="w-5 h-5" />
        </div>
      </div>

      {(subtitle || trend) && (
        <div className="mt-3 pt-3 border-t border-navy-800/80 flex items-center justify-between text-xs">
          {subtitle && <span className="text-slate-400">{subtitle}</span>}
          {trend && (
            <span
              className={`font-semibold ${
                trend.positive ? 'text-emerald-400' : 'text-slate-400'
              }`}
            >
              {trend.label}
            </span>
          )}
        </div>
      )}
    </div>
  );
};
