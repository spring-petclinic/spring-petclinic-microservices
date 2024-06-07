import { useEffect, useState } from 'react';
import { useCollapse } from 'react-collapsed';
import axios from 'axios';
import {
    Button,
    TextField,
    Autocomplete,
    Alert,
    LinearProgress,
    Box,
} from '@mui/material';
import {
    KeyboardDoubleArrowRight as RightArrowIcon,
    KeyboardDoubleArrowDown as DownArrowIcon,
} from '@mui/icons-material';
import { GATLING_RETURN_VALUE_MOCK } from '../../../testing/GatlingMock';
import { GatlingResponse } from './GatlingResponseInterfaces';
import { ErrorDataDisplay } from '../../../utils/ErrorDataDisplay';
import { InputField } from '../../../utils/InputField';

const options = [
    { label: 'Vets Service', path: 'vets' },
    { label: 'Owners Service', path: 'owners' },
    { label: 'Customers Service', path: 'customers' },
];

const ScenarioWithParams = ({ title, text }) => {
    const { getCollapseProps, getToggleProps, isExpanded } = useCollapse();

    const [amountUser, setAmountUser] = useState('');
    const [duration, setDuration] = useState('');
    const [inputValue, setInputValue] = useState('');
    const [data, setData] = useState<GatlingResponse | null>(null);

    const [showSpinner, setShowSpinner] = useState(false);
    const [usersEmpty, setUsersEmpty] = useState(false);
    const [durationEmpty, setDurationEmpty] = useState(false);
    const [serviceEmpty, setServiceEmpty] = useState(false);

    const [mockValueReturned, setMockValueReturned] = useState(false);
    const [progress, setProgress] = useState(0);

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

    const validateInputs = (users: string, duration: string, selectedOption: { label: string; path: string; }) => {
        setUsersEmpty(users === '');
        setDurationEmpty(duration === '');
        setServiceEmpty(selectedOption === undefined);

        return users !== '' && duration !== '' && selectedOption !== undefined;
    };

    const startTest = (users: string, duration: string) => {
        setShowSpinner(true);

        const selectedOption = options.find((option) => option.label === inputValue);
        if (!validateInputs(users, duration, selectedOption)) {
            setShowSpinner(false);
            return;
        }

        const path = selectedOption.path;
        axios
            .get(path, {
                baseURL: process.env.API_URL,
                params: { users, duration },
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

    return (
        <div className="collapsible">
            <div className="header" {...getToggleProps()}>
                {isExpanded ? <DownArrowIcon /> : <RightArrowIcon />} {title}
            </div>
            <div {...getCollapseProps()}>
                <div className="content">
                    <p>{text}</p>
                    <form>
                        <InputField
                            label="Anzahl Nutzer"
                            value={amountUser}
                            onChange={setAmountUser}
                            error={usersEmpty}
                            errorMessage="Bitte Anzahl der Nutzer angeben."
                        />
                        <InputField
                            label="Dauer"
                            value={duration}
                            onChange={setDuration}
                            error={durationEmpty}
                            errorMessage="Bitte eine Dauer angeben."
                        />
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <p>An welchen Service soll der Test gehen:</p>
                            <Autocomplete
                                disablePortal
                                id="combo-box-service"
                                options={options}
                                inputValue={inputValue}
                                onInputChange={(event, newInputValue) => {
                                    setInputValue(newInputValue);
                                }}
                                isOptionEqualToValue={(option, value) => option.label === value.label}
                                sx={{ width: 300 }}
                                renderInput={(params) => (
                                    <TextField {...params} label="Bitte Service auswählen..." />
                                )}
                            />
                            {serviceEmpty && (
                                <Alert severity="error" className="mt-2">
                                    Bitte einen Service auswählen.
                                </Alert>
                            )}
                        </div>
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <Button
                                variant="outlined"
                                className="mb-3"
                                onClick={() => startTest(amountUser, duration)}
                            >
                                Test starten
                            </Button>
                            {showSpinner && 
                                <Box sx={{ width: '100%' }}>
                                    <LinearProgress variant="determinate" value={progress}/>
                                </Box>
                            }
                        </div>
                    </form>
                    <div>
                        {mockValueReturned && <Alert severity="info">Mock value returned</Alert>}
                        <div className="mt-3">
                            {data && <ErrorDataDisplay data={data} />}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ScenarioWithParams;
