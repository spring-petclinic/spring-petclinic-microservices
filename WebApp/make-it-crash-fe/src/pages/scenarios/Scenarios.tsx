import Scenario from "./custom/Scenario"
import ScenarioWithParams from "./custom/ScenarioWithParams"

function Scenarios() {
    return (
            <div>
                <h1 className="fw-bold">Szenarien</h1>
                <ul className="list-unstyled mt-3">
                    <li className="mb-3"><Scenario title="Make Vets Crash!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="vets" duration="20" users=""></Scenario></li>
                    <li className="mb-3"><Scenario title="Make Customers Crash!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="customers" duration="" users=""></Scenario></li>
                    <li className="mb-3"><Scenario title="Make Visits Crash!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="visits" duration="" users=""></Scenario></li>
                    <li className="mb-3"><ScenarioWithParams title="Szenario mit flexiblen Parametern" text="Wähle in diesem Szenario deine bevorzugten Parameter aus."></ScenarioWithParams></li>
                </ul>
            </div>
        )
  }
  
  export default Scenarios