export type Language = "en" | "th";

export type ServiceState = "OPERATIONAL" | "DEGRADED" | "MAINTENANCE";

export interface ServiceStatusResponse {
  status: ServiceState;
  components: { api: "UP" | "DOWN"; database: "UP" | "DOWN" };
  message: string;
  estimatedBackAt: string | null;
  checkedAt: string;
}

export interface ApiErrorBody {
  timestamp: string;
  status: number;
  code: string;
  message: string;
}
