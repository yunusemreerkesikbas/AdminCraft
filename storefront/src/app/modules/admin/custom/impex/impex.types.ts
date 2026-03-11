export interface StatementResult {
    index: number;
    preview: string;
    success: boolean;
    affectedRows: number;
    errorMessage: string | null;
}

export interface ImpExResult {
    status: 'SUCCESS' | 'PARTIAL';
    totalStatements: number;
    executedStatements: number;
    failedStatements: number;
    results: StatementResult[];
    executedAt: string;
}
