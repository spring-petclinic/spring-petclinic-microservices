import Button from '@mui/material/Button';
import axios from 'axios';
import { useCollapse } from "react-collapsed";


function Scenario({ title, text, path }: { title: string, text: string, path: string }) {
    const startTest = () => {
        axios.post(`${process.env.API_URL}/${path}`).then((response) => {
            console.log(response);
        }).catch((error) => {
            console.log(error);
        });
    }

    const { getCollapseProps, getToggleProps } = useCollapse();

    return (
        <div className="collapsible">
            <div className="header" {...getToggleProps()}>
                {/* {isExpanded ? `${title} - Klicke zum Schließen` : `${title} - Klicke zum Erweitern`} */}
                {title}
            </div>
            <div {...getCollapseProps()}>
                <div className="content">
                    <p>{text}</p>
                    <Button variant="outlined" onClick={startTest}>Test starten</Button>
                </div>
            </div>
        </div>
    );
}

export default Scenario