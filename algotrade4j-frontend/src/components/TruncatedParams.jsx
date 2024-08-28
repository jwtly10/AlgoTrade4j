import React from 'react';
import {Box, IconButton, Tooltip, Typography} from '@mui/material';
import ContentCopyIcon from '@mui/icons-material/ContentCopy';

const TruncatedParams = ({params}) => {
    const [tooltipOpen, setTooltipOpen] = React.useState(false);
    const fullParams = JSON.stringify(params, null, 2);
    const truncatedParams = JSON.stringify(params).slice(0, 30) + '...';

    const handleCopy = () => {
        navigator.clipboard.writeText(fullParams);
        setTooltipOpen(false);
    };

    return (
        <Tooltip
            open={tooltipOpen}
            onClose={() => setTooltipOpen(false)}
            onOpen={() => setTooltipOpen(true)}
            title={
                <Box>
                    <Typography component="pre" sx={{
                        whiteSpace: 'pre-wrap',
                        wordBreak: 'break-word',
                        mb: 1
                    }}>
                        {fullParams}
                    </Typography>
                    <IconButton
                        onClick={handleCopy}
                        size="small"
                        sx={{color: 'white'}}
                    >
                        <ContentCopyIcon fontSize="small"/>
                    </IconButton>
                </Box>
            }
        >
            <Typography
                noWrap
                sx={{
                    cursor: 'pointer',
                    maxWidth: 250, // Adjust as needed
                }}
            >
                {truncatedParams}
            </Typography>
        </Tooltip>
    );
};

export default TruncatedParams;