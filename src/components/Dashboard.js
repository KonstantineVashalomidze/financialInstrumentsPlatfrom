import React from 'react';
import styled from 'styled-components';

const DashboardContainer = styled.div`
    display: flex;
    flex-wrap: wrap;
    justify-content: space-between;
`;

const InstrumentCard = styled.div`
    width: 200px;
    padding: 1rem;
    border: 1px solid #ccc;
    border-radius: 4px;
    margin-bottom: 1rem;
    background-color: ${(props) => (props.subscribed == 'true' ? '#d4edda' : '#f8d7da')};
`;

const Dashboard = ({ data, subscriptions }) => {
    return (
        <DashboardContainer>
            {data.map((item) => (
                <InstrumentCard key={item.symbol} subscribed={subscriptions.includes(item.symbol).toString()}>
                    <h3>{item.symbol}</h3>
                    <p>Price: {typeof item.price === 'number' ? item.price.toFixed(2) : 'N/A'}</p>
                    <p>Time: {new Date(item.timeStamp).toLocaleString()}</p>
                </InstrumentCard>
            ))}
        </DashboardContainer>
    );
};

export default Dashboard;