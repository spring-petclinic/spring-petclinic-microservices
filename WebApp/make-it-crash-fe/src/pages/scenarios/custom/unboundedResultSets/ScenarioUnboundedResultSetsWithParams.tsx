import { useState } from 'react';
import { useCollapse } from 'react-collapsed';
import axios from 'axios';
import {
    Button,
    Alert,
    LinearProgress,
    Box,
} from '@mui/material';
import {
    KeyboardDoubleArrowRight as RightArrowIcon,
    KeyboardDoubleArrowDown as DownArrowIcon,
} from '@mui/icons-material';
import { InputField } from '../../../../utils/InputField';



const ScenarioUnboundedResultSetsWithParams = ({ title, text, path }) => {
    const { getCollapseProps, getToggleProps, isExpanded } = useCollapse();

    const [insertCount, setInsertCount] = useState('');
    const [data, setData] = useState(null);

    const [showSpinner, setShowSpinner] = useState(false);
    const [insertCountEmpty, setInsertCountEmpty] = useState(false);

    const validateInputs = (insertCount: string) => {
        setInsertCountEmpty(insertCount === '');

        return insertCount !== '';
    };

    const startTest = (inserts: string) => {
        setShowSpinner(true);

        if (!validateInputs(inserts)) {
            setShowSpinner(false);
            return;
        }

        axios
            .post(`${process.env.API_GW_URL}${path}/init?inserts=${inserts}`, {
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
                setData(response.data);
                console.log(response);
            })
            .catch((error) => {
                setShowSpinner(false);
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
                    <p className="mt-2 fst-italic">{text}</p>
                    <form>
                        <InputField
                            label="Anzahl Inserts"
                            value={insertCount}
                            onChange={setInsertCount}
                            error={insertCountEmpty}
                            errorMessage="Bitte Anzahl der Inserts angeben."
                        />
                        <div className="mb-3 d-flex flex-column align-items-center">
                            <Button
                                variant="outlined"
                                className="mb-3"
                                onClick={() => startTest(insertCount)}
                            >
                                Test starten
                            </Button>
                            {showSpinner && 
                                <Box sx={{ width: '100%' }}>
                                    <LinearProgress/>
                                </Box>
                            }
                        </div>
                    </form>
                    <div>
                        <div className="mt-3">
                            {data && <Alert severity="success" className="mt-2">
                                    Erfolgt!
                                </Alert>}
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default ScenarioUnboundedResultSetsWithParams;
