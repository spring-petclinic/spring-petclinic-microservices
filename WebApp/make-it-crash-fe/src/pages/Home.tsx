import Button from "@mui/material/Button"
import { useNavigate } from "react-router-dom";

function Home() {

    let navigate = useNavigate(); 
    const routeChange = () =>{ 
        let path = `scenarios`; 
        navigate(path);
    }

    return (
        <div>
            <div className="mt-5">
                <h1 className="fw-bold">Willkommen bei "Make It Crash!"</h1>
                <h5 className="mt-2">Bitte wähle dein gewünschtes Testing Szenario</h5>
                <img src="/src/assets/HAW_Marke_CMYK_300dpi.jpg" alt="HAW" className="w-50"/>
            </div>
            <Button className="mt-3" variant="contained" onClick={routeChange}>Zu den Szenarien</Button>
        </div>
        )
  }
  
  export default Home