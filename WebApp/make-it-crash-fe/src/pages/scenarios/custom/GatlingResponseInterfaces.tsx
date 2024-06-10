interface GroupStats {
    name: string;
    count: number;
    percentage: number;
    htmlName: string;
}

interface NumberStats {
    total: number;
    ok: number;
    ko: number;
}

interface ResponseTimeStats {
    total: number;
    ok: number;
    ko: number;
}

interface Percentiles {
    total: number;
    ok: number;
    ko: number;
}

interface RequestStats {
    name: string;
    numberOfRequests: NumberStats;
    minResponseTime: ResponseTimeStats;
    maxResponseTime: ResponseTimeStats;
    meanResponseTime: ResponseTimeStats;
    standardDeviation: ResponseTimeStats;
    percentiles1: Percentiles;
    percentiles2: Percentiles;
    percentiles3: Percentiles;
    percentiles4: Percentiles;
    group1: GroupStats;
    group2: GroupStats;
    group3: GroupStats;
    group4: GroupStats;
    meanNumberOfRequestsPerSecond: NumberStats;
}

interface Request {
    type: string;
    name: string;
    path: string;
    pathFormatted: string;
    stats: RequestStats;
}

export interface GatlingResponse {
    type: string;
    name: string;
    path: string;
    pathFormatted: string;
    stats: RequestStats;
    contents: {
        [key: string]: Request;
    };
}