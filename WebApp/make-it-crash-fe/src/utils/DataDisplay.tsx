import capitalizeWords from "./utils";

export const GatlingDataDisplay = ({ data }) => (
    <div className="error-data-display">
        <h3>Gatling</h3>

        <h5 className="mt-3">Total</h5>
        <p>Mean response time: {data.stats.meanResponseTime.total}</p>
        <p>Total requests: {data.stats.numberOfRequests.total}</p>
        <p>OKs: {data.stats.numberOfRequests.ok}</p>
        <p>KOs: {data.stats.numberOfRequests.ko}</p>


        {Object.keys(data.contents).map((key) => {
            const request = data.contents[key];
            return (
                <div key={key}>
                    <h5 className="mt-5">{capitalizeWords(request.name)}</h5>
                    <p>Mean response time: {request.stats.meanResponseTime.total}</p>
                    <p>Total requests: {request.stats.numberOfRequests.total}</p>
                    <p>OKs: {request.stats.numberOfRequests.ok}</p>
                    <p>KOs: {request.stats.numberOfRequests.ko}</p>
                </div>
            );
        })}

    </div>
);