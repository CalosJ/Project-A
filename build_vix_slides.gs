/**
 * VIX Shock Analysis — Google Slides builder.
 *
 * HOW TO USE
 * 1. Go to https://script.google.com and click "New project".
 * 2. Replace the default Code.gs contents with this entire file.
 * 3. Save (disk icon), then in the function dropdown pick `buildVIXSlides`
 *    and click "Run".
 * 4. The first run will ask for permission to create Sheets/Slides on
 *    your Drive — accept.
 * 5. When it finishes, the Logger (View → Logs, or "Execution log")
 *    prints the URLs of the new spreadsheet and the new presentation.
 *
 * The script builds:
 *   - a Google Sheet with 4 data tabs (yearly counts + SPX response)
 *   - native Sheets charts (line, stacked bar, pie, 100% stacked bars,
 *     grouped bar)
 *   - a Google Slides deck that LINKS those charts so they stay
 *     editable and refreshable from the sheet.
 *
 * Data was extracted from the four text files produced by
 * VIX_Shock_Analysis.java (vix_above_20.txt, vix_10_to_20.txt,
 * vix_below_neg20.txt, vix_neg10_to_neg20.txt) and from running
 * analyzeSPXResponse() against spx_data.csv.
 */

const YEARS = [
  '1996','1997','1998','1999','2000','2001','2002','2003','2004','2005',
  '2006','2007','2008','2009','2010','2011','2012','2013','2014','2015',
  '2016','2017','2018','2019','2020','2021','2022','2023','2024','2025',
];

// Yearly counts per category (key = year string)
const ABOVE_20 = {
  '1997':1,'1998':1,'2001':1,'2002':1,'2005':1,'2006':4,
  '2007':6,'2008':6,'2009':3,'2010':7,'2011':9,'2013':3,
  '2014':7,'2015':6,'2016':4,'2017':4,'2018':12,'2019':5,
  '2020':11,'2021':8,'2022':4,'2024':5,'2025':7,
};
const P10_TO_20 = {
  '1996':7,'1997':9,'1998':19,'1999':13,'2000':14,'2001':11,
  '2002':11,'2003':3,'2004':7,'2005':9,'2006':7,'2007':21,
  '2008':19,'2009':11,'2010':15,'2011':17,'2012':15,'2013':13,
  '2014':17,'2015':24,'2016':14,'2017':8,'2018':20,'2019':16,
  '2020':11,'2021':16,'2022':20,'2023':14,'2024':15,'2025':15,
};
const BELOW_N20 = {
  '2006':1,'2007':2,'2008':3,'2010':1,'2011':1,'2012':1,
  '2013':1,'2016':2,'2017':2,'2018':1,'2020':1,'2024':3,'2025':1,
};
const N10_TO_N20 = {
  '1996':4,'1997':3,'1998':14,'1999':12,'2000':8,'2001':5,
  '2002':6,'2003':1,'2004':4,'2005':4,'2006':7,'2007':13,
  '2008':15,'2009':5,'2010':6,'2011':9,'2012':12,'2013':9,
  '2014':17,'2015':21,'2016':13,'2017':7,'2018':20,'2019':14,
  '2020':11,'2021':24,'2022':5,'2023':7,'2024':10,'2025':10,
};

// SPX response after each VIX shock (Java analyzeSPXResponse output)
const SHOCK_RESULTS = [
  { label:'VIX above 20%',    total:115, day1Up:70,  day1Down:45,  day2Up:53,  day2Down:62,  atLeastOneUp:93,  bothUp:30,  bothDown:22 },
  { label:'VIX 10% to 20%',   total:411, day1Up:227, day1Down:184, day2Up:226, day2Down:185, atLeastOneUp:335, bothUp:118, bothDown:76 },
  { label:'VIX below -20%',   total:20,  day1Up:9,   day1Down:11,  day2Up:12,  day2Down:8,   atLeastOneUp:16,  bothUp:5,   bothDown:4  },
  { label:'VIX -10% to -20%', total:296, day1Up:153, day1Down:143, day2Up:162, day2Down:134, atLeastOneUp:230, bothUp:85,  bothDown:66 },
];

function buildVIXSlides() {
  const ss = SpreadsheetApp.create('VIX Shock Analysis — Data');
  const charts = buildSheetAndCharts_(ss);

  const ssUrl = ss.getUrl();
  const yearlyUrl = ssUrl + '#gid=' + ss.getSheetByName('Yearly counts').getSheetId();
  const respUrl   = ssUrl + '#gid=' + ss.getSheetByName('SPX response').getSheetId();

  const pres = SlidesApp.create('VIX Shock Analysis');
  // Remove the auto-generated default slide so we control everything.
  const initial = pres.getSlides();
  initial.forEach(function (s) { s.remove(); });

  addTitleSlide_(pres, ssUrl);
  addStatsSlide_(pres);
  addChartSlide_(pres, 'Shock events by year (1996–2025)',
    'Counts from vix_above_20.txt, vix_10_to_20.txt, vix_below_neg20.txt, vix_neg10_to_neg20.txt.',
    charts.yearlyLine, yearlyUrl);
  addChartSlide_(pres, 'Stacked yearly composition',
    'All four categories stacked so you can see total shock activity per year.',
    charts.yearlyStacked, yearlyUrl);
  addChartSlide_(pres, 'Share of events by category',
    'Of every VIX shock event in the dataset, which kind was it?',
    charts.pie, yearlyUrl);
  addTwoChartSlide_(pres,
    'S&P 500 response on day i+1 and day i+2',
    '100%-stacked bars: what % of the time did SPX close up vs down after the shock?',
    charts.day1, charts.day2,
    'Day i+1', 'Day i+2', respUrl);
  addChartSlide_(pres, 'Joint outcome over the next two days',
    'Both days up, at least one day up, or both days down — by shock category.',
    charts.joint, respUrl);
  addRawTableSlide_(pres);
  addTakeawaysSlide_(pres);

  Logger.log('Spreadsheet: ' + ssUrl);
  Logger.log('Presentation: ' + pres.getUrl());
}

/* ---------------------------------------------------------------- */
/* Sheet + chart construction                                        */
/* ---------------------------------------------------------------- */

function buildSheetAndCharts_(ss) {
  // Default first sheet -> rename and use for yearly counts.
  const yearly = ss.getSheets()[0].setName('Yearly counts');
  const header = ['Year','VIX up > 20%','VIX up 10–20%','VIX down 10–20%','VIX down > 20%'];
  yearly.getRange(1, 1, 1, header.length).setValues([header]).setFontWeight('bold');
  const rows = YEARS.map(function (y) {
    return [
      y,
      ABOVE_20[y] || 0,
      P10_TO_20[y] || 0,
      N10_TO_N20[y] || 0,
      BELOW_N20[y] || 0,
    ];
  });
  yearly.getRange(2, 1, rows.length, header.length).setValues(rows);
  yearly.setFrozenRows(1);
  yearly.autoResizeColumns(1, header.length);

  // Totals on the same sheet (for the pie chart).
  const totalsStartCol = header.length + 2; // gap of 1 col
  const totalsHeader = [['Category','Events']];
  const totals = [
    ['VIX up > 20%',    sum_(ABOVE_20)],
    ['VIX up 10–20%',   sum_(P10_TO_20)],
    ['VIX down 10–20%', sum_(N10_TO_N20)],
    ['VIX down > 20%',  sum_(BELOW_N20)],
  ];
  yearly.getRange(1, totalsStartCol, 1, 2).setValues(totalsHeader).setFontWeight('bold');
  yearly.getRange(2, totalsStartCol, totals.length, 2).setValues(totals);

  // SPX response sheet
  const resp = ss.insertSheet('SPX response');
  const respHeader = [
    'Shock category','Total events',
    'Up i+1','Down i+1','Up i+2','Down i+2',
    'Both up','At least one up','Both down',
    '% Up i+1','% Down i+1','% Up i+2','% Down i+2',
    '% Both up','% ≥1 up','% Both down',
  ];
  resp.getRange(1, 1, 1, respHeader.length).setValues([respHeader]).setFontWeight('bold');
  const respRows = SHOCK_RESULTS.map(function (r) {
    const p = function (n) { return r.total === 0 ? 0 : +(100 * n / r.total).toFixed(1); };
    return [
      r.label, r.total,
      r.day1Up, r.day1Down, r.day2Up, r.day2Down,
      r.bothUp, r.atLeastOneUp, r.bothDown,
      p(r.day1Up), p(r.day1Down), p(r.day2Up), p(r.day2Down),
      p(r.bothUp), p(r.atLeastOneUp), p(r.bothDown),
    ];
  });
  resp.getRange(2, 1, respRows.length, respHeader.length).setValues(respRows);
  resp.setFrozenRows(1);
  resp.autoResizeColumns(1, respHeader.length);

  /* ---------- Charts ---------- */

  // Line chart: events per year by category.
  const yearlyLine = yearly.newChart()
    .setChartType(Charts.ChartType.LINE)
    .addRange(yearly.getRange(1, 1, YEARS.length + 1, 5))
    .setNumHeaders(1)
    .setPosition(2, 8, 0, 0)
    .setOption('title', 'VIX shock events per year')
    .setOption('width', 900).setOption('height', 420)
    .setOption('legend', { position: 'bottom' })
    .build();
  yearly.insertChart(yearlyLine);

  // Stacked column chart: yearly composition.
  const yearlyStacked = yearly.newChart()
    .setChartType(Charts.ChartType.COLUMN)
    .addRange(yearly.getRange(1, 1, YEARS.length + 1, 5))
    .setNumHeaders(1)
    .setPosition(25, 8, 0, 0)
    .setOption('title', 'Yearly composition of VIX shocks')
    .setOption('isStacked', true)
    .setOption('width', 900).setOption('height', 420)
    .setOption('legend', { position: 'bottom' })
    .build();
  yearly.insertChart(yearlyStacked);

  // Pie chart: share by category.
  const pie = yearly.newChart()
    .setChartType(Charts.ChartType.PIE)
    .addRange(yearly.getRange(1, totalsStartCol, totals.length + 1, 2))
    .setNumHeaders(1)
    .setPosition(48, 8, 0, 0)
    .setOption('title', 'Share of events by shock category')
    .setOption('pieHole', 0.4)
    .setOption('width', 700).setOption('height', 420)
    .setOption('legend', { position: 'right' })
    .build();
  yearly.insertChart(pie);

  // Build dedicated ranges for the SPX response charts so columns line up
  // cleanly. We write small helper blocks.
  const helperStart = respHeader.length + 2;
  resp.getRange(1, helperStart, 1, 3).setValues([['Category','% SPX up','% SPX down']]).setFontWeight('bold');
  // Day i+1
  const day1Block = SHOCK_RESULTS.map(function (r, i) {
    const row = respRows[i];
    return [r.label, row[9], row[10]]; // %Up i+1, %Down i+1
  });
  resp.getRange(2, helperStart, day1Block.length, 3).setValues(day1Block);

  resp.getRange(1, helperStart + 4, 1, 3).setValues([['Category','% SPX up','% SPX down']]).setFontWeight('bold');
  const day2Block = SHOCK_RESULTS.map(function (r, i) {
    const row = respRows[i];
    return [r.label, row[11], row[12]]; // %Up i+2, %Down i+2
  });
  resp.getRange(2, helperStart + 4, day2Block.length, 3).setValues(day2Block);

  resp.getRange(1, helperStart + 8, 1, 4).setValues([['Category','% Both up','% ≥1 up','% Both down']]).setFontWeight('bold');
  const jointBlock = SHOCK_RESULTS.map(function (r, i) {
    const row = respRows[i];
    return [r.label, row[13], row[14], row[15]]; // %Both up, %≥1 up, %Both down
  });
  resp.getRange(2, helperStart + 8, jointBlock.length, 4).setValues(jointBlock);

  const day1 = resp.newChart()
    .setChartType(Charts.ChartType.BAR)
    .addRange(resp.getRange(1, helperStart, day1Block.length + 1, 3))
    .setNumHeaders(1)
    .setPosition(10, 1, 0, 0)
    .setOption('title', 'SPX response on day i+1 (% of events)')
    .setOption('isStacked', 'percent')
    .setOption('width', 700).setOption('height', 360)
    .setOption('legend', { position: 'bottom' })
    .setOption('hAxis', { format: '#\'%\'' })
    .build();
  resp.insertChart(day1);

  const day2 = resp.newChart()
    .setChartType(Charts.ChartType.BAR)
    .addRange(resp.getRange(1, helperStart + 4, day2Block.length + 1, 3))
    .setNumHeaders(1)
    .setPosition(30, 1, 0, 0)
    .setOption('title', 'SPX response on day i+2 (% of events)')
    .setOption('isStacked', 'percent')
    .setOption('width', 700).setOption('height', 360)
    .setOption('legend', { position: 'bottom' })
    .setOption('hAxis', { format: '#\'%\'' })
    .build();
  resp.insertChart(day2);

  const joint = resp.newChart()
    .setChartType(Charts.ChartType.COLUMN)
    .addRange(resp.getRange(1, helperStart + 8, jointBlock.length + 1, 4))
    .setNumHeaders(1)
    .setPosition(50, 1, 0, 0)
    .setOption('title', 'Joint outcome over the next two days')
    .setOption('width', 900).setOption('height', 420)
    .setOption('legend', { position: 'bottom' })
    .setOption('vAxis', { format: '#\'%\'' })
    .build();
  resp.insertChart(joint);

  SpreadsheetApp.flush();
  return { yearlyLine: yearlyLine, yearlyStacked: yearlyStacked, pie: pie, day1: day1, day2: day2, joint: joint };
}

/* ---------------------------------------------------------------- */
/* Slide helpers                                                     */
/* ---------------------------------------------------------------- */

function addTitleSlide_(pres, ssUrl) {
  const slide = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addTextBox_(slide, 'VIX Shock Analysis', 40, 140, 880, 80, 40, true);
  addTextBox_(slide,
    'Daily VIX percentage-change shocks (1996–2025) and how the S&P 500 reacted on the next two trading days.',
    40, 240, 880, 80, 16, false, '#666666');
  addTextBox_(slide, 'Source: VIX_Shock_Analysis.java • vix_data.csv • spx_data.csv',
    40, 460, 880, 30, 12, false, '#999999');
  if (ssUrl) {
    addLiveLink_(slide, 'Open live data sheet →', ssUrl, 40, 500, 240, 20);
  }
}

function addStatsSlide_(pres) {
  const slide = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addTextBox_(slide, 'Summary', 40, 30, 880, 50, 28, true);

  const cards = [
    { label: 'Total shock events', value: sum_(ABOVE_20) + sum_(P10_TO_20) + sum_(N10_TO_N20) + sum_(BELOW_N20) },
    { label: 'VIX up > 20%',       value: sum_(ABOVE_20) },
    { label: 'VIX up 10–20%',      value: sum_(P10_TO_20) },
    { label: 'VIX down > 10%',     value: sum_(N10_TO_N20) + sum_(BELOW_N20) },
  ];
  const cardWidth = 200, cardHeight = 110, gap = 20;
  const startX = (960 - (cardWidth * 4 + gap * 3)) / 2;
  cards.forEach(function (c, i) {
    const x = startX + i * (cardWidth + gap);
    const y = 130;
    addTextBox_(slide, String(c.value), x, y, cardWidth, 60, 36, true);
    addTextBox_(slide, c.label, x, y + 65, cardWidth, 40, 13, false, '#666666');
  });

  addTextBox_(slide,
    'A "shock" is a trading day where VIX moved more than ±10% versus the prior close. ' +
    'Events were bucketed into four categories.',
    40, 290, 880, 80, 14, false, '#444444');
}

function addChartSlide_(pres, title, subtitle, chart, liveUrl) {
  const slide = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addTextBox_(slide, title, 40, 24, 880, 40, 22, true);
  if (subtitle) addTextBox_(slide, subtitle, 40, 64, 880, 30, 12, false, '#666666');
  const inserted = slide.insertSheetsChart(chart);
  inserted.setLeft(60).setTop(110).setWidth(840).setHeight(370);
  if (liveUrl) {
    addLiveLink_(slide, 'Open in Sheets for live hover tooltips →', liveUrl, 640, 500, 280, 20);
  }
}

function addTwoChartSlide_(pres, title, subtitle, chartA, chartB, labelA, labelB, liveUrl) {
  const slide = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addTextBox_(slide, title, 40, 24, 880, 40, 22, true);
  if (subtitle) addTextBox_(slide, subtitle, 40, 64, 880, 30, 12, false, '#666666');

  addTextBox_(slide, labelA, 60, 110, 410, 24, 14, true);
  const a = slide.insertSheetsChart(chartA);
  a.setLeft(60).setTop(140).setWidth(410).setHeight(330);

  addTextBox_(slide, labelB, 490, 110, 410, 24, 14, true);
  const b = slide.insertSheetsChart(chartB);
  b.setLeft(490).setTop(140).setWidth(410).setHeight(330);

  if (liveUrl) {
    addLiveLink_(slide, 'Open in Sheets for live hover tooltips →', liveUrl, 640, 500, 280, 20);
  }
}

function addRawTableSlide_(pres) {
  const slide = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addTextBox_(slide, 'Raw counts', 40, 24, 880, 40, 22, true);
  addTextBox_(slide, 'Mirror of the values printed by analyzeSPXResponse() in VIX_Shock_Analysis.java.',
    40, 64, 880, 24, 12, false, '#666666');

  const headers = ['Shock category','Total','Up i+1','Down i+1','Up i+2','Down i+2','Both up','Both down','≥1 up'];
  const rows = SHOCK_RESULTS.map(function (r) {
    return [r.label, r.total, r.day1Up, r.day1Down, r.day2Up, r.day2Down, r.bothUp, r.bothDown, r.atLeastOneUp];
  });

  const numRows = rows.length + 1;
  const numCols = headers.length;
  const table = slide.insertTable(numRows, numCols, 40, 120, 880, 260);
  for (let c = 0; c < numCols; c++) {
    table.getCell(0, c).getText().setText(headers[c]).getTextStyle().setBold(true);
  }
  for (let r = 0; r < rows.length; r++) {
    for (let c = 0; c < numCols; c++) {
      table.getCell(r + 1, c).getText().setText(String(rows[r][c]));
    }
  }
}

function addTakeawaysSlide_(pres) {
  const slide = pres.appendSlide(SlidesApp.PredefinedLayout.BLANK);
  addTextBox_(slide, 'Takeaways', 40, 30, 880, 50, 28, true);

  const points = [
    'Large positive VIX spikes (>20%) are followed by an SPX up close on day i+1 ~61% of the time — the classic post-shock relief bounce.',
    'On day i+2 after a >20% VIX spike, SPX skews slightly negative (53 up vs 62 down) — the bounce often fades.',
    'Big VIX drops below −20% are rare (only 20 events in 30 years) and SPX direction is essentially a coin flip after them.',
    'For the milder 10–20% VIX moves, SPX is close to balanced (~55% up on i+1, ~55% up on i+2).',
    '2015, 2018, and 2021 stand out as the busiest years for VIX shocks overall.',
  ];
  const startY = 110, lineHeight = 60;
  points.forEach(function (p, i) {
    addTextBox_(slide, '•  ' + p, 60, startY + i * lineHeight, 860, 56, 14, false, '#222222');
  });
}

/* ---------------------------------------------------------------- */
/* Utilities                                                         */
/* ---------------------------------------------------------------- */

function addTextBox_(slide, text, x, y, w, h, fontSize, bold, color) {
  const box = slide.insertTextBox(text, x, y, w, h);
  const style = box.getText().getTextStyle();
  style.setFontSize(fontSize);
  style.setBold(!!bold);
  if (color) style.setForegroundColor(color);
  return box;
}

function addLiveLink_(slide, text, url, x, y, w, h) {
  const box = slide.insertTextBox(text, x, y, w, h);
  const style = box.getText().getTextStyle();
  style.setFontSize(11);
  style.setBold(false);
  style.setForegroundColor('#1a73e8');
  style.setUnderline(true);
  style.setLinkUrl(url);
  return box;
}

function sum_(obj) {
  let s = 0;
  for (const k in obj) s += obj[k];
  return s;
}
