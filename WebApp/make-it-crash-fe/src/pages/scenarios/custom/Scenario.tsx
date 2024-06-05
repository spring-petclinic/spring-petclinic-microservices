import Button from '@mui/material/Button';
import axios from 'axios';
import { useCollapse } from "react-collapsed";
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';
import KeyboardDoubleArrowDownIcon from '@mui/icons-material/KeyboardDoubleArrowDown';

function Scenario({ title, text, path }: { title: string, text: string, path: string }) {
    const startTest = () => {
        axios.get(`${process.env.API_URL}/${path}`).then((response) => { // TODO: need's to be fixed (best case outsourced to a service)
            console.log(response);
        }).catch((error) => {
            console.log(error);
        });
    }

    const { getCollapseProps, getToggleProps, isExpanded } = useCollapse();

    return (
        <div className="collapsible">
            <div className="header" {...getToggleProps()}>
                {!isExpanded && <KeyboardDoubleArrowRightIcon />}
                {isExpanded && <KeyboardDoubleArrowDownIcon />}
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