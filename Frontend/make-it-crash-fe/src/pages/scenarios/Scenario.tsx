import { Button } from "react-bootstrap"
import axios from 'axios';

function Scenario({text, path}: {text: string, path: string}) {
    const startTest = () => {
        axios.post(`${process.env.API_URL}/${path}`).then((response) => {
            console.log(response);
        }).catch((error) => {
            console.log(error);
        });
    }

    return (
        <div>
            <Button variant="text" onClick={startTest}>{text}</Button>
        </div>
        )
  }
  
  export default Scenario