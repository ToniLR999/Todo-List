export interface SystemMetrics {
    memory: {
      used: number;
      max: number;
      percentage: number;
    };
    cpu: {
      usage: number;
      load: number;
    };
    database: {
      connections: number;
      maxConnections: number;
      status: string;
    };
    redis: {
      status: string;
      operations: number;
    };
    app: {
      status: string;
      uptime: number;
      schedule: string;
      version: string;
    };
  }
  