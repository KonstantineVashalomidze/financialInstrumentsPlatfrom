import React from 'react';
import { Grid, Card, CardContent, Typography, Box } from '@mui/material';
import { styled } from '@mui/material/styles';

const StyledCard = styled(Card)(({ theme, subscribed }) => ({
    height: '100%',
    display: 'flex',
    flexDirection: 'column',
    backgroundColor: subscribed.toString() === 'true' ? theme.palette.success.light : theme.palette.error.light,
    boxShadow: '0 4px 8px 0 rgba(0, 0, 0, 0.5)',

}));

const Dashboard = ({ data, subscriptions }) => {
    return (
        <Box sx={{ flexGrow: 1, p: 3 }}>
            <Grid container spacing={3}>
                {data.map((item) => (
                    <Grid item xs={12} sm={6} md={4} lg={3} key={item.symbol}>
                        <StyledCard subscribed={subscriptions.includes(item.symbol)}>
                            <CardContent>
                                <Typography variant="h5" component="div" gutterBottom>
                                    {item.symbol}
                                </Typography>
                                <Typography variant="body1" color="text.secondary">
                                    Price: {typeof item.price === 'number' ? item.price.toFixed(2) : 'N/A'}
                                </Typography>
                                <Typography variant="body2" color="text.secondary">
                                    Time: {new Date(item.timeStamp).toLocaleString()}
                                </Typography>
                            </CardContent>
                        </StyledCard>
                    </Grid>
                ))}
            </Grid>
        </Box>
    );
};

export default Dashboard;