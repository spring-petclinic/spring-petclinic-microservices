import Button from '@mui/material/Button';
import axios from 'axios';
import { useCollapse } from "react-collapsed";
import KeyboardDoubleArrowRightIcon from '@mui/icons-material/KeyboardDoubleArrowRight';
import KeyboardDoubleArrowDownIcon from '@mui/icons-material/KeyboardDoubleArrowDown';
import { useEffect, useState } from 'react';
import { GATLING_RETURN_VALUE_MOCK } from '../../../testing/GatlingMock';
import { GatlingResponse } from './GatlingResponseInterfaces';
import { Alert, Box, LinearProgress } from '@mui/material';
import { ErrorDataDisplay } from '../../../utils/ErrorDataDisplay';

function Scenario({ title, text, path, duration, users }: { title: string, text: string, path: string, duration: string, users: string }) {
    const [showSpinner, setShowSpinner] = useState(false);
    const [mockValueReturned, setMockValueReturned] = useState(false);
    const [progress, setProgress] = useState(0);
    const [data, setData] = useState<GatlingResponse | null>(null);
    
    useEffect(() => {
        if (showSpinner) {
            const totalSteps = (parseInt(duration) + 1) * 10;
            const increment = 100 / totalSteps;
            let steps = 0;

            const timer = setInterval(() => {
                setProgress((oldProgress) => {
                    if (steps >= totalSteps) {
                        clearInterval(timer);
                        return 100;
                    }
                    steps += 1;
                    return Math.min(oldProgress + increment, 100);
                });
            }, 100);

            return () => {
                clearInterval(timer);
                setProgress(0);
            };
        }
    }, [showSpinner]);
    
    const startTest = () => {
        setShowSpinner(true);

        axios
            .get(path, {
                baseURL: process.env.API_URL,
                params: { users, duration},
                responseType: 'json',
                headers: { 'content-type': 'application/json' },
                proxy: {
                    protocol: 'http',
                    host: '127.0.0.1',
                    port: 8081,
                  },
            })
            .then((response) => {
                setShowSpinner(false);
                if (response.status == 200 && response.data == 'Error executing Gatling test.') {
                    console.error(response);
                    return;
                }
                setData(response.data);
                setMockValueReturned(false);
                console.log(response);
            })
            .catch((error) => {
                setShowSpinner(false);
                setData(GATLING_RETURN_VALUE_MOCK);
                setMockValueReturned(true);
                console.error(error);
            });
    };

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
                    <p className="mt-2 fst-italic">{text}</p>
                    <Button variant="outlined" onClick={startTest}>Test starten</Button>

                    {showSpinner && 
                                <Box className="mt-3" sx={{ width: '100%' }}>
                                    <LinearProgress variant="determinate" value={progress}/>
                                </Box>
                            }

                    {mockValueReturned && <Alert className="mt-3" severity="info">Mock value returned</Alert>}
                    <div className="mt-3">
                        {data && <ErrorDataDisplay data={data} />}
                    </div>
                </div>
            </div>
        </div>
    );
}

export default Scenario