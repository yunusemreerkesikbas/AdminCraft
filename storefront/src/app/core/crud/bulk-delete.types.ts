/** Mirrors backend bulk-delete payload and result (read-only on the client). */
export interface BulkDeleteRequest {
  ids: number[];
}

export interface BulkDeleteError {
  id: number;
  code: string;
  message: string;
}

export interface BulkDeleteResult {
  requestedCount: number;
  deletedIds: number[];
  failedIds: number[];
  errors: BulkDeleteError[];
}
