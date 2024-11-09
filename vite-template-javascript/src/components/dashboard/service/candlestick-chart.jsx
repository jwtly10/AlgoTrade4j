'use client';

import * as React from 'react';
import Avatar from '@mui/material/Avatar';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import CardHeader from '@mui/material/CardHeader';
import Stack from '@mui/material/Stack';
import { Wallet as WalletIcon } from '@phosphor-icons/react/dist/ssr/Wallet';

import TradingViewChart from './tradingview-chart';

export function CandlestickChart({ backtestConfiguration, data }) {
  const showChart = true;
  return (
    <Card>
      <CardHeader
        avatar={
          <Avatar>
            <WalletIcon fontSize="var(--Icon-fontSize)" />
          </Avatar>
        }
        subheader="Balance across all your accounts"
        title="Current balance"
      />
      <CardContent>
        {/* <Stack direction="row" spacing={3} sx={{ alignItems: 'center', flexWrap: 'wrap' }}> */}
        <TradingViewChart
          showChart={showChart}
          strategyConfig={backtestConfiguration}
          chartData={{}}
          trades={[]}
          indicators={{}}
        />
        {/* </Stack> */}
      </CardContent>
    </Card>
  );
}
