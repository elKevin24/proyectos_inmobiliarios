import '../styles/Dashboard.css';

const StatCard = ({ title, value, icon, color, subtitle, trend }) => {
  const getColorClass = () => {
    const colors = {
      blue: 'stat-card-blue',
      green: 'stat-card-green',
      orange: 'stat-card-orange',
      purple: 'stat-card-purple',
      red: 'stat-card-red',
      teal: 'stat-card-teal'
    };
    return colors[color] || 'stat-card-blue';
  };

  return (
    <div className={`stat-card ${getColorClass()}`}>
      <div className="stat-icon">{icon}</div>
      <div className="stat-content">
        <h3 className="stat-title">{title}</h3>
        <p className="stat-value">{value}</p>
        {subtitle && <p className="stat-subtitle">{subtitle}</p>}
        {trend && (
          <p className={`stat-trend ${trend.direction}`}>
            {trend.direction === 'up' ? '↑' : '↓'} {trend.value}
          </p>
        )}
      </div>
    </div>
  );
};

export default StatCard;
