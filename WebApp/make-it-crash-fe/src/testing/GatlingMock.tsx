import { GatlingResponse } from "../pages/scenarios/custom/GatlingResponseInterfaces";

export const GATLING_RETURN_VALUE_MOCK: GatlingResponse = {
    "type": "GROUP",
    "name": "All Requests",
    "path": "",
    "pathFormatted": "group_missing-name--1146707516",
    "stats": {
        "name": "All Requests",
        "numberOfRequests": {
            "total": 1800,
            "ok": 1800,
            "ko": 0
        },
        "minResponseTime": {
            "total": 3,
            "ok": 3,
            "ko": 0
        },
        "maxResponseTime": {
            "total": 180,
            "ok": 180,
            "ko": 0
        },
        "meanResponseTime": {
            "total": 10,
            "ok": 10,
            "ko": 0
        },
        "standardDeviation": {
            "total": 9,
            "ok": 9,
            "ko": 0
        },
        "percentiles1": {
            "total": 8,
            "ok": 8,
            "ko": 0
        },
        "percentiles2": {
            "total": 11,
            "ok": 11,
            "ko": 0
        },
        "percentiles3": {
            "total": 17,
            "ok": 17,
            "ko": 0
        },
        "percentiles4": {
            "total": 37,
            "ok": 37,
            "ko": 0
        },
        "group1": {
            "name": "t < 800 ms",
            "htmlName": "t < 800 ms",
            "count": 1800,
            "percentage": 100
        },
        "group2": {
            "name": "800 ms <= t < 1200 ms",
            "htmlName": "t >= 800 ms <br> t < 1200 ms",
            "count": 0,
            "percentage": 0
        },
        "group3": {
            "name": "t >= 1200 ms",
            "htmlName": "t >= 1200 ms",
            "count": 0,
            "percentage": 0
        },
        "group4": {
            "name": "failed",
            "htmlName": "failed",
            "count": 0,
            "percentage": 0
        },
        "meanNumberOfRequestsPerSecond": {
            "total": 60.0,
            "ok": 60.0,
            "ko": 0
        }
    },
    "contents": {
        "req_owners--1003854816": {
            "type": "REQUEST",
            "name": "owners",
            "path": "owners",
            "pathFormatted": "req_owners--1003854816",
            "stats": {
                "name": "owners",
                "numberOfRequests": {
                    "total": 900,
                    "ok": 900,
                    "ko": 0
                },
                "minResponseTime": {
                    "total": 3,
                    "ok": 3,
                    "ko": 0
                },
                "maxResponseTime": {
                    "total": 171,
                    "ok": 171,
                    "ko": 0
                },
                "meanResponseTime": {
                    "total": 11,
                    "ok": 11,
                    "ko": 0
                },
                "standardDeviation": {
                    "total": 8,
                    "ok": 8,
                    "ko": 0
                },
                "percentiles1": {
                    "total": 10,
                    "ok": 10,
                    "ko": 0
                },
                "percentiles2": {
                    "total": 12,
                    "ok": 12,
                    "ko": 0
                },
                "percentiles3": {
                    "total": 17,
                    "ok": 17,
                    "ko": 0
                },
                "percentiles4": {
                    "total": 33,
                    "ok": 33,
                    "ko": 0
                },
                "group1": {
                    "name": "t < 800 ms",
                    "htmlName": "t < 800 ms",
                    "count": 900,
                    "percentage": 100
                },
                "group2": {
                    "name": "800 ms <= t < 1200 ms",
                    "htmlName": "t >= 800 ms <br> t < 1200 ms",
                    "count": 0,
                    "percentage": 0
                },
                "group3": {
                    "name": "t >= 1200 ms",
                    "htmlName": "t >= 1200 ms",
                    "count": 0,
                    "percentage": 0
                },
                "group4": {
                    "name": "failed",
                    "htmlName": "failed",
                    "count": 0,
                    "percentage": 0
                },
                "meanNumberOfRequestsPerSecond": {
                    "total": 30.0,
                    "ok": 30.0,
                    "ko": 0
                }
            }
        }, 
        "req_vets-3616110": {
            "type": "REQUEST",
            "name": "vets",
            "path": "vets",
            "pathFormatted": "req_vets-3616110",
            "stats": {
                "name": "vets",
                "numberOfRequests": {
                    "total": 900,
                    "ok": 900,
                    "ko": 0
                },
                "minResponseTime": {
                    "total": 3,
                    "ok": 3,
                    "ko": 0
                },
                "maxResponseTime": {
                    "total": 180,
                    "ok": 180,
                    "ko": 0
                },
                "meanResponseTime": {
                    "total": 9,
                    "ok": 9,
                    "ko": 0
                },
                "standardDeviation": {
                    "total": 10,
                    "ok": 10,
                    "ko": 0
                },
                "percentiles1": {
                    "total": 7,
                    "ok": 7,
                    "ko": 0
                },
                "percentiles2": {
                    "total": 9,
                    "ok": 9,
                    "ko": 0
                },
                "percentiles3": {
                    "total": 16,
                    "ok": 16,
                    "ko": 0
                },
                "percentiles4": {
                    "total": 49,
                    "ok": 49,
                    "ko": 0
                },
                "group1": {
                    "name": "t < 800 ms",
                    "htmlName": "t < 800 ms",
                    "count": 900,
                    "percentage": 100
                },
                "group2": {
                    "name": "800 ms <= t < 1200 ms",
                    "htmlName": "t >= 800 ms <br> t < 1200 ms",
                    "count": 0,
                    "percentage": 0
                },
                "group3": {
                    "name": "t >= 1200 ms",
                    "htmlName": "t >= 1200 ms",
                    "count": 0,
                    "percentage": 0
                },
                "group4": {
                    "name": "failed",
                    "htmlName": "failed",
                    "count": 0,
                    "percentage": 0
                },
                "meanNumberOfRequestsPerSecond": {
                    "total": 30.0,
                    "ok": 30.0,
                    "ko": 0
                }
            }
        }
    }
};