import ScenarioUnbalancedCapacities from "./custom/unbalancedCapacities/ScenarioUnbalancedCapacities"
import ScenarioUnboundedResultSetsInit from "./custom/unboundedResultSets/ScenarioUnboundedResultSetsInit"
import ScenarioUnbalancedCapacitiesWithParams from "./custom/unbalancedCapacities/ScenarioUnbalancedCapacitiesWithParams"
import ScenarioUnboundedResultSetsDelete from "./custom/unboundedResultSets/ScenarioUnboundedResultSetsDelete"
import ScenarioUnboundedResultSetsWithParams from "./custom/unboundedResultSets/ScenarioUnboundedResultSetsWithParams"
import {Divider} from '@mui/material';

function Scenarios() {
    return (
            <div>
                <h1 className="fw-bold">Szenarien</h1>
                <div className="mt-3">
                    <h3>Unbalanced Capacities</h3>
                    <ul className="list-unstyled mt-3">
                        <li className="mb-3"><ScenarioUnbalancedCapacities title="Make Vets Crash!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="vets" duration="5" users="100"></ScenarioUnbalancedCapacities></li>
                        <li className="mb-3"><ScenarioUnbalancedCapacities title="Make Customers Crash!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="customers" duration="5" users="100"></ScenarioUnbalancedCapacities></li>
                        <li className="mb-3"><ScenarioUnbalancedCapacities title="Make Visits Crash!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="visits" duration="5" users="100"></ScenarioUnbalancedCapacities></li>
                        <li className="mb-3"><ScenarioUnbalancedCapacitiesWithParams title="Szenario mit flexiblen Parametern" text="Wähle in diesem Szenario deine bevorzugten Parameter aus."></ScenarioUnbalancedCapacitiesWithParams></li>
                    </ul>
                </div>
                <Divider className="mt-2"/>
                <div className="mt-2">
                    <h3>Unbounden Result Sets</h3>
                    <h5>Owners</h5>
                    <ul className="list-unstyled mt-3">
                        <li className="mb-3"><ScenarioUnboundedResultSetsInit title="Erstelle 50.000 User!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="customer/owners" inserts="50000"></ScenarioUnboundedResultSetsInit></li>
                        <li className="mb-3"><ScenarioUnboundedResultSetsWithParams title="Szenario mit flexiblen Parametern" text="Wähle in diesem Szenario deine bevorzugten Parameter aus." path="customer/owners"></ScenarioUnboundedResultSetsWithParams></li>
                        <li className="mb-3"><ScenarioUnboundedResultSetsDelete title="Lösche alle User!" text="Achtung: Es werden alle User gelöscht!" path="customer/owners"></ScenarioUnboundedResultSetsDelete></li>
                    </ul>
                    <h5>Vets</h5>
                    <ul className="list-unstyled mt-3">
                        <li className="mb-3"><ScenarioUnboundedResultSetsInit title="Erstelle 50.000 Vets!" text="Der Service wird ausgelastet und reagiert für eine gewisse Zeit nicht mehr." path="vet/vets" inserts="50000"></ScenarioUnboundedResultSetsInit></li>
                        <li className="mb-3"><ScenarioUnboundedResultSetsWithParams title="Szenario mit flexiblen Parametern" text="Wähle in diesem Szenario deine bevorzugten Parameter aus." path="vet/vets"></ScenarioUnboundedResultSetsWithParams></li>
                        <li className="mb-3"><ScenarioUnboundedResultSetsDelete title="Lösche alle Vets!" text="Achtung: Es werden alle Vets gelöscht!" path="vet/vets"></ScenarioUnboundedResultSetsDelete></li>
                    </ul>
                </div>
            </div>
        )
  }
  
  export default Scenarios