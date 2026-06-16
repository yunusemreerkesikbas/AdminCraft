export type ApiResponse<T> = {
  result: "SUCCESS" | "ERROR";
  message?: string;
  data?: T;
};
