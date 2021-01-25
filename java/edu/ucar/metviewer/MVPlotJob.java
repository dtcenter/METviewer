package edu.ucar.metviewer;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import edu.ucar.metviewer.db.DatabaseManager;

import static edu.ucar.metviewer.MVUtil.PYTHON;
import static edu.ucar.metviewer.MVUtil.RSCRIPT;

/**
 * Storage class for a xml plot specification, implementing the java bean interface.  Instances are
 * populated by the MVPlotJobParser and handled by MVBatch.
 */
public class MVPlotJob {

  protected String jobName = "";

  protected List<String> currentDBName;

  protected String rscript = "";

  protected String plotTmpl = "";

  protected String indyVar = "";
  protected String[] indyVal = {};
  protected String[] indyPlotVal = {};
  protected String[] indyLabel = {};


  protected MVPlotDep depIndy = null;

  protected MVOrderedMap plotFixVal = new MVOrderedMap();
  protected MVOrderedMap plotFixValEq = new MVOrderedMap();

  protected MVOrderedMap[] depGroup = {};

  protected MVOrderedMap series1Val = new MVOrderedMap();
  protected MVOrderedMap series2Val = new MVOrderedMap();

  protected MVOrderedMap seriesNobs = new MVOrderedMap();

  protected MVOrderedMap dep1Scale = new MVOrderedMap();
  protected MVOrderedMap dep2Scale = new MVOrderedMap();

  protected MVOrderedMap tmplMaps = new MVOrderedMap();
  protected MVOrderedMap tmplVal = new MVOrderedMap();

  protected String dataFileTmpl = "";
  protected String plotFileTmpl = "";
  protected String rFileTmpl = "";
  protected String titleTmpl = "";
  protected String xLabelTmpl = "";
  protected String y1LabelTmpl = "";
  protected String y2LabelTmpl = "";
  protected String captionTmpl = "";
  protected String jobTitleTmpl = "";
  protected boolean keepRevisions = false;
  protected String plotCmd = ""; // inserted and executed in Rscript
  protected String plotCond = ""; // insert and execute in database queries

  protected boolean _boolEventEqual = false;
  protected boolean _boolVertPlot = false;
  protected boolean _boolXReverse = false;
  protected boolean _boolShowNStats = false;
  protected boolean _boolIndy1Stagger = false;
  protected boolean _boolIndy2Stagger = false;
  protected boolean _boolGridOn = false;
  protected boolean _boolSyncAxes = false;
  protected boolean _boolDumpPoints1 = false;
  protected boolean _boolDumpPoints2 = false;
  protected boolean _boolLogY1 = false;
  protected boolean _boolLogY2 = false;
  protected boolean _boolVarianceInflationFactor = true;
  protected boolean _boolNormalizedHistogram = true;
  protected boolean _boolCacheAggStat = false;

  protected String _strPlotType = "png256";
  protected String _strPlotHeight = "8.5";
  protected String _strPlotWidth = "11";
  protected String _strPlotRes = "72";
  protected String _strPlotUnits = "in";
  protected String _strMar = "c(8, 4, 5, 4)";
  protected String _strMgp = "c(1, 1, 0)";
  protected String _strCex = "1";
  protected String _strTitleWeight = "2";
  protected String _strTitleSize = "1";
  protected String _strTitleOffset = "-2.5";
  protected String _strTitleAlign = ".5";
  protected String _strXtlabOrient = "1";
  protected String _strXtlabPerp = "-.75";
  protected String _strXtlabHoriz = ".5";
  protected String _strXtlabFreq = "0";
  protected String _strXtlabSize = "1";
  protected String _strXlabWeight = "1";
  protected String _strXlabSize = "1";
  protected String _strXlabOffset = "2";
  protected String _strXlabAlign = ".5";
  protected String _strYtlabOrient = "1";
  protected String _strYtlabPerp = ".5";
  protected String _strYtlabHoriz = ".5";
  protected String _strYtlabSize = "1";
  protected String _strYlabWeight = "1";
  protected String _strYlabSize = "1";
  protected String _strYlabOffset = "-2";
  protected String _strYlabAlign = ".5";
  protected String _strGridLty = "3";
  protected String _strGridCol = "#CCCCCC";
  protected String _strGridLwd = "1";
  protected String _strGridX = "listX";
  protected String _strX2tlabOrient = "1";
  protected String _strX2tlabPerp = "1";
  protected String _strX2tlabHoriz = ".5";
  protected String _strX2tlabSize = ".8";
  protected String _strX2labWeight = "1";
  protected String _strX2labSize = ".8";
  protected String _strX2labOffset = "-.5";
  protected String _strX2labAlign = ".5";
  protected String _strY2tlabOrient = "1";
  protected String _strY2tlabPerp = ".5";
  protected String _strY2tlabHoriz = ".5";
  protected String _strY2tlabSize = "1";
  protected String _strY2labWeight = "1";
  protected String _strY2labSize = "1";
  protected String _strY2labOffset = "1";
  protected String _strY2labAlign = ".5";
  protected String _strLegendSize = ".8";
  protected String _strLegendBox = "o";
  protected String _strLegendInset = "c(0, -.25)";
  protected String _strLegendNcol = "3";
  protected String _strCaptionWeight = "1";
  protected String _strCaptionCol = "#999999";
  protected String _strCaptionSize = ".8";
  protected String _strCaptionOffset = "3";
  protected String _strCaptionAlign = ".5";
  protected String _strBoxPts = "FALSE";
  protected String _strBoxBoxwex = ".2";
  protected String _strBoxOutline = "TRUE";
  protected String _strBoxNotch = "FALSE";
  protected String _strBoxAvg = "FALSE";
  protected String _strRelyEventHist = "TRUE";
  protected String _strCIAlpha = ".05";
  protected String _strEnsSsPts = "-1";
  protected String _strEnsSsPtsDisp = "TRUE";

  protected boolean _boolAggCtc = false;
  protected boolean _boolAggNbrCtc = false;
  protected boolean _boolAggSl1l2 = false;
  protected boolean _boolAggGrad = false;
  protected boolean _boolAggSal1l2 = false;
  protected boolean _boolAggPct = false;
  protected boolean _boolAggNbrCnt = false;
  protected boolean _boolAggSsvar = false;
  protected boolean _boolAggVl1l2 = false;
  protected boolean _boolAggVal1l2 = false;
  protected boolean _boolAggEcnt = false;
  protected boolean _boolAggRps = false;
  protected String _strAggBootRepl = "1";
  protected String _strAggBootRandomSeed = "NA";

  protected Float cl_step = 0.05F;

  protected String _strAggBootCI = "bca";
  protected boolean _boolEveqDis = false;
  protected boolean _equalizeByIndep = true;

  protected boolean _boolCalcCtc = false;
  protected boolean _boolCalcSl1l2 = false;
  protected boolean _boolCalcGrad = false;
  protected boolean _boolCalcSal1l2 = false;
  protected boolean _boolCalcVl1l2 = false;
  protected boolean _boolCalcVal1l2 = false;

  protected boolean revisionStat = false;
  protected boolean revisionAc = false;
  protected boolean revisionRun = false;

  protected boolean _boolRocPct = false;
  protected boolean _boolRocCtc = false;

  protected boolean _boolTaylorVoc = true;
  protected boolean _boolTaylorShowGamma = true;

  protected String _strPlotCI = "";
  protected String _strPlotDisp = "";
  protected String _strColors = "";
  protected String _strPch = "";
  protected String _strType = "";
  protected String _strLty = "";
  protected String _strLwd = "";
  protected String _strConSeries = "";
  protected String _strLegend = "";
  protected String _strY1Lim = "";
  protected String _strX1Lim = "";
  protected String _strY1Bufr = ".04";
  protected String _strY2Lim = "";
  protected String _strY2Bufr = ".04";
  protected String _plotStat = "median";
  protected String _strOrderSeries = "";
  protected String _strDiffSeries1 = "list()";
  protected String _strDiffSeries2 = "list()";
  protected String _strShowSignif = "";
  protected List<String> summaryCurve = new ArrayList<>();
  protected boolean addPointThresholds = Boolean.TRUE;
  protected boolean addSkillLine = Boolean.TRUE;
  protected boolean insetHist = Boolean.FALSE;
  protected boolean addNoSkillLine = Boolean.TRUE;
  protected boolean addReferenceLine = Boolean.TRUE;
  protected Boolean isModeRatio = null;
  protected Boolean isMtdRatio = null;
  protected Boolean isMode = null;
  protected Boolean isMtd = null;
  protected Boolean isAddColorBar = Boolean.TRUE;
  protected Boolean isReverseY = Boolean.FALSE;
  protected Boolean isReverseX = Boolean.FALSE;
  protected Boolean isAddContourOverlay = Boolean.TRUE;
  protected Integer contourIntervals = 10;
  protected String colorPalette = "cm.colors";
  protected String executionType = RSCRIPT;
  protected String annotationTemplate = "";
  protected boolean createHtml = Boolean.FALSE;
  protected double eqboundLow = -0.001;
  protected double eqboundHigh = -0.001;
  protected Boolean isCircularBlockBootstrap = Boolean.TRUE;



  /**
   * Deep copy of the MVPlotJob, useful for inheritance.
   *
   * @return Copy of this MVPlotJob
   */
  public MVPlotJob copy() {
    MVPlotJob job = new MVPlotJob();

    job.jobName = jobName + "_copy";
    job.currentDBName = currentDBName;

    job.rscript = rscript;
    job.plotTmpl = plotTmpl;
    job.indyVar = indyVar;
    job.indyVal = MVUtil.copyList(indyVal);
    job.indyPlotVal = MVUtil.copyList(indyPlotVal);
    job.indyLabel = MVUtil.copyList(indyLabel);
    job.depIndy = depIndy;
    job.plotFixVal = new MVOrderedMap(plotFixVal);
    job.plotFixValEq = new MVOrderedMap(plotFixValEq);
    job.depGroup = MVUtil.copyList(depGroup);
    job.series1Val = new MVOrderedMap(series1Val, "s");
    job.series2Val = new MVOrderedMap(series2Val, "s");


    job.seriesNobs = new MVOrderedMap(seriesNobs);
    job.dep1Scale = new MVOrderedMap(dep1Scale);
    job.dep2Scale = new MVOrderedMap(dep2Scale);
    job.tmplMaps = new MVOrderedMap(tmplMaps);
    job.tmplVal = new MVOrderedMap(tmplVal);

    job.dataFileTmpl = dataFileTmpl;
    job.plotFileTmpl = plotFileTmpl;
    job.rFileTmpl = rFileTmpl;
    job.titleTmpl = titleTmpl;
    job.xLabelTmpl = xLabelTmpl;
    job.y1LabelTmpl = y1LabelTmpl;
    job.y2LabelTmpl = y2LabelTmpl;
    job.captionTmpl = captionTmpl;
    job.jobTitleTmpl = jobTitleTmpl;
    job.plotCmd = plotCmd;
    job.plotCond = plotCond;

    job._boolEventEqual = _boolEventEqual;
    job._boolVertPlot = _boolVertPlot;
    job._boolXReverse = _boolXReverse;
    job._boolShowNStats = _boolShowNStats;
    job._boolIndy1Stagger = _boolIndy1Stagger;
    job._boolIndy2Stagger = _boolIndy2Stagger;
    job._boolGridOn = _boolGridOn;
    job._boolSyncAxes = _boolSyncAxes;
    job._boolDumpPoints1 = _boolDumpPoints1;
    job._boolDumpPoints2 = _boolDumpPoints2;
    job._boolLogY1 = _boolLogY1;
    job._boolLogY2 = _boolLogY2;
    job._boolVarianceInflationFactor = _boolVarianceInflationFactor;
    job._boolNormalizedHistogram = _boolNormalizedHistogram;
    job._boolCacheAggStat = _boolCacheAggStat;
    job._boolTaylorVoc = _boolTaylorVoc;
    job._boolTaylorShowGamma = _boolTaylorShowGamma;

    job._strPlotType = _strPlotType;
    job._strPlotHeight = _strPlotHeight;
    job._strPlotWidth = _strPlotWidth;
    job._strPlotRes = _strPlotRes;
    job._strPlotUnits = _strPlotUnits;
    job._strMar = _strMar;
    job._strMgp = _strMgp;
    job._strCex = _strCex;
    job._strTitleWeight = _strTitleWeight;
    job._strTitleSize = _strTitleSize;
    job._strTitleOffset = _strTitleOffset;
    job._strTitleAlign = _strTitleAlign;
    job._strXtlabOrient = _strXtlabOrient;
    job._strXtlabPerp = _strXtlabPerp;
    job._strXtlabHoriz = _strXtlabHoriz;
    job._strXtlabFreq = _strXtlabFreq;
    job._strXtlabSize = _strXtlabSize;
    job._strXlabWeight = _strXlabWeight;
    job._strXlabSize = _strXlabSize;
    job._strXlabOffset = _strXlabOffset;
    job._strXlabAlign = _strXlabAlign;
    job._strYtlabOrient = _strYtlabOrient;
    job._strYtlabPerp = _strYtlabPerp;
    job._strYtlabHoriz = _strYtlabHoriz;
    job._strYtlabSize = _strYtlabSize;
    job._strYlabWeight = _strYlabWeight;
    job._strYlabSize = _strYlabSize;
    job._strYlabOffset = _strYlabOffset;
    job._strYlabAlign = _strYlabAlign;
    job._strGridLty = _strGridLty;
    job._strGridCol = _strGridCol;
    job._strGridLwd = _strGridLwd;
    job._strGridX = _strGridX;
    job._strX2tlabOrient = _strX2tlabOrient;
    job._strX2tlabPerp = _strX2tlabPerp;
    job._strX2tlabHoriz = _strX2tlabHoriz;
    job._strX2tlabSize = _strX2tlabSize;
    job._strX2labWeight = _strX2labWeight;
    job._strX2labSize = _strX2labSize;
    job._strX2labOffset = _strX2labOffset;
    job._strX2labAlign = _strX2labAlign;
    job._strY2tlabOrient = _strY2tlabOrient;
    job._strY2tlabPerp = _strY2tlabPerp;
    job._strY2tlabHoriz = _strY2tlabHoriz;
    job._strY2tlabSize = _strY2tlabSize;
    job._strY2labWeight = _strY2labWeight;
    job._strY2labSize = _strY2labSize;
    job._strY2labOffset = _strY2labOffset;
    job._strY2labAlign = _strY2labAlign;
    job._strLegendSize = _strLegendSize;
    job._strLegendBox = _strLegendBox;
    job._strLegendInset = _strLegendInset;
    job._strLegendNcol = _strLegendNcol;
    job._strCaptionWeight = _strCaptionWeight;
    job._strCaptionCol = _strCaptionCol;
    job._strCaptionSize = _strCaptionSize;
    job._strCaptionOffset = _strCaptionOffset;
    job._strCaptionAlign = _strCaptionAlign;
    job._strBoxOutline = _strBoxOutline.toUpperCase(Locale.US);
    job._strBoxPts = _strBoxPts.toUpperCase(Locale.US);
    job._strBoxBoxwex = _strBoxBoxwex;
    job._strBoxNotch = _strBoxNotch.toUpperCase(Locale.US);
    job._strBoxAvg = _strBoxAvg.toUpperCase(Locale.US);
    job._strRelyEventHist = _strRelyEventHist;
    job._strCIAlpha = _strCIAlpha;
    job._strEnsSsPts = _strEnsSsPts;
    job._strEnsSsPtsDisp = _strEnsSsPtsDisp;

    job._boolAggCtc = _boolAggCtc;
    job._boolAggNbrCtc = _boolAggNbrCtc;
    job._boolAggSl1l2 = _boolAggSl1l2;
    job._boolAggGrad = _boolAggGrad;
    job._boolAggSal1l2 = _boolAggSal1l2;
    job._boolAggPct = _boolAggPct;
    job._boolAggNbrCnt = _boolAggNbrCnt;
    job._boolAggSsvar = _boolAggSsvar;
    job._boolAggEcnt = _boolAggEcnt;
    job._boolAggRps = _boolAggRps;
    job._strAggBootRepl = _strAggBootRepl;
    job._strAggBootRandomSeed = _strAggBootRandomSeed;
    job.cl_step = cl_step;
    job._strAggBootCI = _strAggBootCI;
    job._boolAggVl1l2 = _boolAggVl1l2;
    job._boolAggVal1l2 = _boolAggVal1l2;
    job._boolEveqDis = _boolEveqDis;
    job._equalizeByIndep = _equalizeByIndep;

    job._boolCalcCtc = _boolCalcCtc;
    job._boolCalcSl1l2 = _boolCalcSl1l2;
    job._boolCalcGrad = _boolCalcGrad;
    job._boolCalcSal1l2 = _boolCalcSal1l2;
    job._boolCalcVl1l2 = _boolCalcVl1l2;
    job._boolCalcVal1l2 = _boolCalcVal1l2;

    job.revisionStat = revisionStat;
    job.revisionAc = revisionAc;
    job.revisionRun = revisionRun;

    job._boolRocPct = _boolRocPct;
    job._boolRocCtc = _boolRocCtc;

    job._strPlotCI = _strPlotCI;
    job._strPlotDisp = _strPlotDisp;
    job._strShowSignif = _strShowSignif;
    job._strDiffSeries1 = _strDiffSeries1;
    job._strDiffSeries2 = _strDiffSeries2;
    job._strOrderSeries = _strOrderSeries;
    job._strColors = _strColors;
    job._strPch = _strPch;
    job._strType = _strType;
    job._strLty = _strLty;
    job._strLwd = _strLwd;
    job._strConSeries = _strConSeries;
    job._strLegend = _strLegend;
    job._strY1Lim = _strY1Lim;
    job._strX1Lim = _strX1Lim;
    job._strY1Bufr = _strY1Bufr;
    job._strY2Lim = _strY2Lim;
    job._strY2Bufr = _strY2Bufr;
    job._plotStat = _plotStat;
    job.addPointThresholds = addPointThresholds;
    job.addSkillLine = addSkillLine;
    job.addNoSkillLine = addNoSkillLine;
    job.addReferenceLine = addReferenceLine;
    job.createHtml = createHtml;
    job.insetHist = insetHist;
    job.summaryCurve.addAll(summaryCurve);

    job.isAddColorBar = isAddColorBar;
    job.isReverseY = isReverseY;
    job.isAddContourOverlay = isAddContourOverlay;
    job.isCircularBlockBootstrap = isCircularBlockBootstrap;
    job.isReverseX = isReverseX;
    job.contourIntervals = contourIntervals;
    job.colorPalette = colorPalette;
    job.executionType = executionType;
    job.annotationTemplate = annotationTemplate;

    job.keepRevisions = keepRevisions;

    job.eqboundLow = eqboundLow;
    job.eqboundHigh = eqboundHigh;
    return job;
  }

  public double getEqboundLow() {
    return eqboundLow;
  }

  public void setEqboundLow(double eqboundLow) {
    this.eqboundLow = eqboundLow;
  }
  public void setEqboundLow(String eqboundLow) {
    try {
      double le = Double.parseDouble(eqboundLow);
      this.eqboundLow = le;
    }catch (Exception e){}

  }

  public double getEqboundHigh() {
    return eqboundHigh;
  }

  public void setEqboundHigh(double eqboundHigh) {
    this.eqboundHigh = eqboundHigh;
  }
  public void setEqboundHigh(String eqboundHigh) {
    try {
      double he = Double.parseDouble(eqboundHigh);
      this.eqboundHigh = he;
    }catch (Exception e){}
  }

  public String getJobName() {
    return jobName;
  }

  public void setJobName(String jobName) {
    this.jobName = jobName;
  }


  public String getRscript() {
    return rscript;
  }

  public void setRscript(String Rscript) {
    rscript = Rscript;
  }

  public String getPlotTmpl() {
    return plotTmpl;
  }

  public void setPlotTmpl(String plotTmpl) {
    this.plotTmpl = plotTmpl;
  }

  public String getIndyVar() {
    return indyVar;
  }

  public void setIndyVar(String indyVar) throws DatabaseException {
    validateSQL(indyVar);
    this.indyVar = indyVar;
  }

  public String[] getIndyVal() {
    return Arrays.copyOf(indyVal, indyVal.length);
  }

  public void setIndyVal(String[] indyVal) throws DatabaseException {
    for (String ind : indyVal) {
      validateSQL(ind);
    }
    this.indyVal = Arrays.copyOf(indyVal, indyVal.length);
  }

  public String[] getIndyPlotVal() {
    return Arrays.copyOf(indyPlotVal, indyPlotVal.length);
  }

  public void setIndyPlotVal(String[] indyPlotVal) throws DatabaseException {
    for (String ind : indyPlotVal) {
      validateSQL(ind);
    }
    this.indyPlotVal = Arrays.copyOf(indyPlotVal, indyPlotVal.length);
  }

  public String[] getIndyLabel() {
    return Arrays.copyOf(indyLabel, indyLabel.length);
  }

  public void setIndyLabel(String[] indyLabel) {
    this.indyLabel = Arrays.copyOf(indyLabel, indyLabel.length);
  }


  public MVPlotDep getIndyDep() {
    return depIndy;
  }

  public void setIndyDep(MVPlotDep dep) {
    depIndy = dep;
  }

  public MVOrderedMap getPlotFixVal() {
    return plotFixVal;
  }

  public MVOrderedMap getPlotFixValEq() {
    return plotFixValEq;
  }

  public void setPlotFixValEq(MVOrderedMap plotFixValEq) {
    this.plotFixValEq = plotFixValEq;
  }

  public void addPlotFixVal(String field, String[] vals, int index) throws DatabaseException {
    for (String ind : vals) {
      validateSQL(ind);
    }
    plotFixVal.put(field, vals, index);
  }

  public void addPlotFixValEq(String field, String[] vals, int index) {
    plotFixValEq.put(field, vals, index);
  }

  public void addPlotFixVal(String field, String[] vals) throws DatabaseException {
    addPlotFixVal(field, vals, plotFixVal.size());
  }

  public void addPlotFixValEq(String field, String[] vals) throws DatabaseException {
    for (String ind : vals) {
      validateSQL(ind);
    }
    addPlotFixValEq(field, vals, plotFixValEq.size());
  }

  public void addPlotFixVal(String field, MVOrderedMap sets, int index) {
    plotFixVal.put(field, sets, index);
  }

  public void addPlotFixValEq(String field, MVOrderedMap sets, int index) {
    //ignore fcst_var
    if (!field.equalsIgnoreCase("fcst_var")) {
      plotFixValEq.put(field, sets, index);
    }
  }

  public void addPlotFixVal(String field, MVOrderedMap sets) {
    addPlotFixVal(field, sets, plotFixVal.size());
  }

  public void addPlotFixValEq(String field, MVOrderedMap sets) {
    //ignore fcst_var
    if (!field.equalsIgnoreCase("fcst_var")) {
      addPlotFixValEq(field, sets, plotFixValEq.size());
    }
  }

  public void removePlotFixVal(String field) {
    plotFixVal.remove(field);
  }


  public void clearPlotFixVal() {
    plotFixVal = new MVOrderedMap();
  }

  public MVOrderedMap[] getDepGroups() {
    return Arrays.copyOf(depGroup, depGroup.length);
  }

  public void addDepGroup(MVOrderedMap depGroup) {
    ArrayList listDepGroup = new ArrayList(Arrays.asList(this.depGroup));
    listDepGroup.add(depGroup);
    this.depGroup = (MVOrderedMap[]) listDepGroup.toArray(new MVOrderedMap[]{});
  }

  public void clearDepGroups() {
    depGroup = new MVOrderedMap[]{};
  }

  public MVOrderedMap getSeries1Val() {
    return series1Val;
  }


  public void addSeries1Val(String field, String[] vals, int index) throws DatabaseException {
    for (String ind : vals) {
      validateSQL(ind);
    }
    series1Val.putSeries(field, vals, index);
  }


  public void addSeries1Val(String field, String[] vals) throws DatabaseException {
    if (field.equals("valid_hour") || field.equals("init_hour")) {
      String[] newVals = new String[vals.length];
      for (int i = 0; i < vals.length; i++) {
        newVals[i] = vals[i].replaceFirst("^0+(?!$)", "");
      }
      vals = newVals;
    }
    addSeries1Val(field, vals, series1Val.size());

  }


  public void removeSeries1Val(String field) {
    series1Val.remove(field);
  }


  public void clearSeries1Val() {
    series1Val = new MVOrderedMap();
  }


  public MVOrderedMap getSeries2Val() {
    return series2Val;
  }

  public void addSeries2Val(String field, String[] vals, int index) throws DatabaseException {
    for (String ind : vals) {
      validateSQL(ind);
    }
    series2Val.putSeries(field, vals, index);
  }

  public void addSeries2Val(String field, String[] vals) throws DatabaseException {
    addSeries2Val(field, vals, series2Val.size());
  }


  public void clearSeries2Val() {
    series2Val = new MVOrderedMap();
  }

  public MVOrderedMap getSeriesNobs() {
    return seriesNobs;
  }

  public void addSeriesNobs(String field, String val, int index) throws DatabaseException {
    validateSQL(val);

    seriesNobs.put(field, val, index);
  }

  public void addSeriesNobs(String field, String val) throws DatabaseException {
    addSeriesNobs(field, val, seriesNobs.size());
  }

  public void removeSeriesNobs(String field) {
    seriesNobs.remove(field);
  }

  public void clearSeriesNobs() {
    seriesNobs = new MVOrderedMap();
  }

  public MVOrderedMap getDep1Scale() {
    return dep1Scale;
  }

  public void addDep1Scale(String field, String val) {
    dep1Scale.put(field, val);
  }

  public void removeDep1Scale(String field) {
    dep1Scale.remove(field);
  }

  public void clearDep1Scale() {
    dep1Scale = new MVOrderedMap();
  }

  public MVOrderedMap getDep2Scale() {
    return dep2Scale;
  }

  public void addDep2Scale(String field, String val) {
    dep2Scale.put(field, val);
  }

  public void removeDep2Scale(String field) {
    dep2Scale.remove(field);
  }

  public void clearDep2Scale() {
    dep2Scale = new MVOrderedMap();
  }

  public MVOrderedMap getTmplMaps() {
    return tmplMaps;
  }

  public MVOrderedMap getTmplMap(String field) {
    if (tmplMaps.containsKey(field)) {
      return (MVOrderedMap) tmplMaps.get(field);
    } else {
      return null;
    }
  }

  public void addTmplMap(String field, MVOrderedMap map, int index) {
    tmplMaps.put(field, map, index);
  }

  public void addTmplMap(String field, MVOrderedMap map) {
    addTmplMap(field, map, tmplMaps.size());
  }


  public MVOrderedMap getTmplVal() {
    return tmplVal;
  }

  public void setTmplVal(MVOrderedMap _mapTmplVal) {
    this.tmplVal = _mapTmplVal;
  }

  public void addTmplVal(String id, String name) {
    tmplVal.put(id, name);
  }

  public String getDataFileTmpl() {
    return dataFileTmpl;
  }

  public void setDataFileTmpl(String dataFileTmpl) {
    this.dataFileTmpl = dataFileTmpl;
  }

  public String getPlotFileTmpl() {
    return plotFileTmpl;
  }

  public void setPlotFileTmpl(String plotFileTmpl) {
    this.plotFileTmpl = plotFileTmpl;
  }

  public String getRFileTmpl() {
    return rFileTmpl;
  }

  public void setRFileTmpl(String rFileTmpl) {
    this.rFileTmpl = rFileTmpl;
  }

  public String getTitleTmpl() {
    return titleTmpl;
  }

  public void setTitleTmpl(String titleTmpl) {
    this.titleTmpl = titleTmpl;
  }

  public String getXLabelTmpl() {
    return xLabelTmpl;
  }

  public void setXLabelTmpl(String xLabelTmpl) {
    this.xLabelTmpl = xLabelTmpl;
  }

  public String getY1LabelTmpl() {
    return y1LabelTmpl;
  }

  public void setY1LabelTmpl(String Y1LabelTmpl) {
    y1LabelTmpl = Y1LabelTmpl;
  }

  public String getY2LabelTmpl() {
    return y2LabelTmpl;
  }

  public void setY2LabelTmpl(String Y2LabelTmpl) {
    y2LabelTmpl = Y2LabelTmpl;
  }

  public String getCaptionTmpl() {
    return captionTmpl;
  }

  public void setCaptionTmpl(String captionTmpl) {
    this.captionTmpl = captionTmpl;
  }

  public String getJobTitleTmpl() {
    return jobTitleTmpl;
  }

  public void setJobTitleTmpl(String jobTitleTmpl) {
    this.jobTitleTmpl = jobTitleTmpl;
  }

  public String getPlotCmd() {
    return plotCmd;
  }

  public void setPlotCmd(String plotCmd) {
    this.plotCmd = plotCmd.replace("\n", ";").replace("\r", ";");
  }

  public String getPlotCond() {
    return plotCond;
  }

  public void setPlotCond(String plotCond) throws DatabaseException {
    validateSQL(plotCond);

    this.plotCond = plotCond;
  }

  public boolean getEventEqual() {
    return _boolEventEqual;
  }

  public void setEventEqual(Boolean eventEqual) {
    _boolEventEqual = eventEqual;
  }


  public boolean getVertPlot() {
    return _boolVertPlot;
  }

  public void setVertPlot(boolean vertPlot) {
    _boolVertPlot = vertPlot;
  }

  public boolean getXReverse() {
    return _boolXReverse;
  }

  public void setXReverse(boolean xReverse) {
    _boolXReverse = xReverse;
  }


  public boolean getShowNStats() {
    return _boolShowNStats;
  }

  public void setShowNStats(boolean showNStats) {
    _boolShowNStats = showNStats;
  }

  public boolean getIndy1Stagger() {
    return _boolIndy1Stagger;
  }

  public void setIndy1Stagger(boolean indy1Stagger) {
    _boolIndy1Stagger = indy1Stagger;
  }

  public boolean getIndy2Stagger() {
    return _boolIndy2Stagger;
  }

  public void setIndy2Stagger(boolean indy2Stagger) {
    _boolIndy2Stagger = indy2Stagger;
  }

  public boolean getGridOn() {
    return _boolGridOn;
  }

  public void setGridOn(boolean gridOn) {
    _boolGridOn = gridOn;
  }

  public boolean getSyncAxes() {
    return _boolSyncAxes;
  }

  public void setSyncAxes(boolean syncAxes) {
    _boolSyncAxes = syncAxes;
  }

  public boolean getDumpPoints1() {
    return _boolDumpPoints1;
  }

  public void setDumpPoints1(boolean dumpPoints1) {
    _boolDumpPoints1 = dumpPoints1;
  }

  public boolean getDumpPoints2() {
    return _boolDumpPoints2;
  }

  public void setDumpPoints2(boolean dumpPoints2) {
    _boolDumpPoints2 = dumpPoints2;
  }

  public boolean getLogY1() {
    return _boolLogY1;
  }

  public void setLogY1(boolean logY1) {
    _boolLogY1 = logY1;
  }

  public boolean getLogY2() {
    return _boolLogY2;
  }

  public void setLogY2(boolean logY2) {
    _boolLogY2 = logY2;
  }

  public String getPlotType() {
    return _strPlotType;
  }

  public void setPlotType(String plotType) {
    _strPlotType = plotType;
  }

  public String getPlotHeight() {
    return _strPlotHeight;
  }

  public void setPlotHeight(String plotHeight) {
    _strPlotHeight = plotHeight;
  }

  public String getPlotWidth() {
    return _strPlotWidth;
  }

  public void setPlotWidth(String plotWidth) {
    _strPlotWidth = plotWidth;
  }

  public String getPlotRes() {
    return _strPlotRes;
  }

  public void setPlotRes(String plotRes) {
    _strPlotRes = plotRes;
  }

  public String getPlotUnits() {
    return _strPlotUnits;
  }

  public void setPlotUnits(String plotUnits) {
    _strPlotUnits = plotUnits;
  }

  public String getMar() {
    return _strMar;
  }

  public void setMar(String mar) {
    _strMar = mar;
  }

  public String getMgp() {
    return _strMgp;
  }

  public void setMgp(String mgp) {
    _strMgp = mgp;
  }

  public String getCex() {
    return _strCex;
  }

  public void setCex(String cex) {
    _strCex = cex;
  }

  public String getTitleWeight() {
    return _strTitleWeight;
  }

  public void setTitleWeight(String titleWeight) {
    _strTitleWeight = titleWeight;
  }

  public String getTitleSize() {
    return _strTitleSize;
  }

  public void setTitleSize(String titleSize) {
    _strTitleSize = titleSize;
  }

  public String getTitleOffset() {
    return _strTitleOffset;
  }

  public void setTitleOffset(String titleOffset) {
    _strTitleOffset = titleOffset;
  }

  public String getTitleAlign() {
    return _strTitleAlign;
  }

  public void setTitleAlign(String titleAlign) {
    _strTitleAlign = titleAlign;
  }

  public String getXtlabOrient() {
    return _strXtlabOrient;
  }

  public void setXtlabOrient(String xtlabOrient) {
    _strXtlabOrient = xtlabOrient;
  }

  public String getXtlabPerp() {
    return _strXtlabPerp;
  }

  public void setXtlabPerp(String xtlabPerp) {
    _strXtlabPerp = xtlabPerp;
  }

  public String getXtlabHoriz() {
    return _strXtlabHoriz;
  }

  public void setXtlabHoriz(String xtlabHoriz) {
    _strXtlabHoriz = xtlabHoriz;
  }

  public String getXtlabFreq() {
    return _strXtlabFreq;
  }

  public void setXtlabFreq(String xtlabFreq) {
    _strXtlabFreq = xtlabFreq;
  }

  public String getXtlabSize() {
    return _strXtlabSize;
  }

  public void setXtlabSize(String xtlabSize) {
    _strXtlabSize = xtlabSize;
  }

  public String getXlabWeight() {
    return _strXlabWeight;
  }

  public void setXlabWeight(String xlabWeight) {
    _strXlabWeight = xlabWeight;
  }

  public String getXlabSize() {
    return _strXlabSize;
  }

  public void setXlabSize(String xlabSize) {
    _strXlabSize = xlabSize;
  }

  public String getXlabOffset() {
    return _strXlabOffset;
  }

  public void setXlabOffset(String xlabOffset) {
    _strXlabOffset = xlabOffset;
  }

  public String getXlabAlign() {
    return _strXlabAlign;
  }

  public void setXlabAlign(String xlabAlign) {
    _strXlabAlign = xlabAlign;
  }

  public String getYtlabOrient() {
    return _strYtlabOrient;
  }

  public void setYtlabOrient(String ytlabOrient) {
    _strYtlabOrient = ytlabOrient;
  }

  public String getYtlabPerp() {
    return _strYtlabPerp;
  }

  public void setYtlabPerp(String ytlabPerp) {
    _strYtlabPerp = ytlabPerp;
  }

  public String getYtlabHoriz() {
    return _strYtlabHoriz;
  }

  public void setYtlabHoriz(String ytlabHoriz) {
    _strYtlabHoriz = ytlabHoriz;
  }

  public String getYtlabSize() {
    return _strYtlabSize;
  }

  public void setYtlabSize(String ytlabSize) {
    _strYtlabSize = ytlabSize;
  }

  public String getYlabWeight() {
    return _strYlabWeight;
  }

  public void setYlabWeight(String ylabWeight) {
    _strYlabWeight = ylabWeight;
  }

  public String getYlabSize() {
    return _strYlabSize;
  }

  public void setYlabSize(String ylabSize) {
    _strYlabSize = ylabSize;
  }

  public String getYlabOffset() {
    return _strYlabOffset;
  }

  public void setYlabOffset(String ylabOffset) {
    _strYlabOffset = ylabOffset;
  }

  public String getYlabAlign() {
    return _strYlabAlign;
  }

  public void setYlabAlign(String ylabAlign) {
    _strYlabAlign = ylabAlign;
  }

  public String getGridLty() {
    return _strGridLty;
  }

  public void setGridLty(String gridLty) {
    _strGridLty = gridLty;
  }

  public String getGridCol() {
    return _strGridCol;
  }

  public void setGridCol(String gridCol) {
    _strGridCol = gridCol;
  }

  public String getGridLwd() {
    return _strGridLwd;
  }

  public void setGridLwd(String gridLwd) {
    _strGridLwd = gridLwd;
  }

  public String getGridX() {
    return _strGridX;
  }

  public void setGridX(String gridX) {
    _strGridX = gridX;
  }

  public String getX2tlabOrient() {
    return _strX2tlabOrient;
  }

  public void setX2tlabOrient(String x2tlabOrient) {
    _strX2tlabOrient = x2tlabOrient;
  }

  public String getX2tlabPerp() {
    return _strX2tlabPerp;
  }

  public void setX2tlabPerp(String x2tlabPerp) {
    _strX2tlabPerp = x2tlabPerp;
  }

  public String getX2tlabHoriz() {
    return _strX2tlabHoriz;
  }

  public void setX2tlabHoriz(String x2tlabHoriz) {
    _strX2tlabHoriz = x2tlabHoriz;
  }

  public String getX2tlabSize() {
    return _strX2tlabSize;
  }

  public void setX2tlabSize(String x2tlabSize) {
    _strX2tlabSize = x2tlabSize;
  }

  public String getX2labWeight() {
    return _strX2labWeight;
  }

  public void setX2labWeight(String x2labWeight) {
    _strX2labWeight = x2labWeight;
  }

  public String getX2labSize() {
    return _strX2labSize;
  }

  public void setX2labSize(String x2labSize) {
    _strX2labSize = x2labSize;
  }

  public String getX2labOffset() {
    return _strX2labOffset;
  }

  public void setX2labOffset(String x2labOffset) {
    _strX2labOffset = x2labOffset;
  }

  public String getX2labAlign() {
    return _strX2labAlign;
  }

  public void setX2labAlign(String x2labAlign) {
    _strX2labAlign = x2labAlign;
  }

  public String getY2tlabOrient() {
    return _strY2tlabOrient;
  }

  public void setY2tlabOrient(String y2tlabOrient) {
    _strY2tlabOrient = y2tlabOrient;
  }

  public String getY2tlabPerp() {
    return _strY2tlabPerp;
  }

  public void setY2tlabPerp(String y2tlabPerp) {
    _strY2tlabPerp = y2tlabPerp;
  }

  public String getY2tlabHoriz() {
    return _strY2tlabHoriz;
  }

  public void setY2tlabHoriz(String y2tlabHoriz) {
    _strY2tlabHoriz = y2tlabHoriz;
  }

  public String getY2tlabSize() {
    return _strY2tlabSize;
  }

  public void setY2tlabSize(String y2tlabSize) {
    _strY2tlabSize = y2tlabSize;
  }

  public String getY2labWeight() {
    return _strY2labWeight;
  }

  public void setY2labWeight(String y2labWeight) {
    _strY2labWeight = y2labWeight;
  }

  public String getY2labSize() {
    return _strY2labSize;
  }

  public void setY2labSize(String y2labSize) {
    _strY2labSize = y2labSize;
  }

  public String getY2labOffset() {
    return _strY2labOffset;
  }

  public void setY2labOffset(String y2labOffset) {
    _strY2labOffset = y2labOffset;
  }

  public String getY2labAlign() {
    return _strY2labAlign;
  }

  public void setY2labAlign(String y2labAlign) {
    _strY2labAlign = y2labAlign;
  }

  public String getLegendSize() {
    return _strLegendSize;
  }

  public void setLegendSize(String legendSize) {
    _strLegendSize = legendSize;
  }

  public String getLegendBox() {
    return _strLegendBox;
  }

  public void setLegendBox(String legendBox) {
    _strLegendBox = legendBox;
  }

  public String getLegendInset() {
    return _strLegendInset;
  }

  public void setLegendInset(String legendInset) {
    _strLegendInset = legendInset;
  }

  public String getLegendNcol() {
    return _strLegendNcol;
  }

  public void setLegendNcol(String legendNcol) {
    _strLegendNcol = legendNcol;
  }

  public String getCaptionWeight() {
    return _strCaptionWeight;
  }

  public void setCaptionWeight(String captionWeight) {
    _strCaptionWeight = captionWeight;
  }

  public String getCaptionCol() {
    return _strCaptionCol;
  }

  public void setCaptionCol(String captionCol) {
    _strCaptionCol = captionCol;
  }

  public String getCaptionSize() {
    return _strCaptionSize;
  }

  public void setCaptionSize(String captionSize) {
    _strCaptionSize = captionSize;
  }

  public String getCaptionOffset() {
    return _strCaptionOffset;
  }

  public void setCaptionOffset(String captionOffset) {
    _strCaptionOffset = captionOffset;
  }

  public String getCaptionAlign() {
    return _strCaptionAlign;
  }

  public void setCaptionAlign(String captionAlign) {
    _strCaptionAlign = captionAlign;
  }

  public String getBoxOutline() {
    return _strBoxOutline;
  }

  public void setBoxOutline(String boxOutline) {
    _strBoxOutline = boxOutline.toUpperCase(Locale.US);
  }

  public String getBoxPts() {
    return _strBoxPts;
  }

  public void setBoxPts(String boxPts) {
    _strBoxPts = boxPts.toUpperCase(Locale.US);
  }

  public String getBoxBoxwex() {
    return _strBoxBoxwex;
  }

  public void setBoxBoxwex(String boxBoxwex) {
    _strBoxBoxwex = boxBoxwex;
  }

  public String getBoxNotch() {
    return _strBoxNotch;
  }

  public void setBoxNotch(String boxNotch) {
    _strBoxNotch = boxNotch.toUpperCase(Locale.US);
  }

  public String getBoxAvg() {
    return _strBoxAvg;
  }

  public void setBoxAvg(String boxAvg) {
    _strBoxAvg = boxAvg.toUpperCase(Locale.US);
  }

  public String getRelyEventHist() {
    return _strRelyEventHist.toUpperCase(Locale.US);
  }

  public void setRelyEventHist(String relyEventHist) {
    _strRelyEventHist = relyEventHist;
  }

  public String getCIAlpha() {
    return _strCIAlpha;
  }

  public void setCIAlpha(String ciAlpha) {
    _strCIAlpha = ciAlpha;
  }

  public String getEnsSsPts() {
    return _strEnsSsPts;
  }

  public void setEnsSsPts(String ensSsPts) {
    _strEnsSsPts = ensSsPts;
  }

  public String getEnsSsPtsDisp() {
    return _strEnsSsPtsDisp;
  }

  public void setEnsSsPtsDisp(String ensSsPtsDisp) {
    _strEnsSsPtsDisp = ensSsPtsDisp.toUpperCase(Locale.US);
  }

  public boolean getAggCtc() {
    return _boolAggCtc;
  }

  public void setAggCtc(boolean aggCtc) {
    _boolAggCtc = aggCtc;
  }

  public boolean getAggSl1l2() {
    return _boolAggSl1l2;
  }

  public void setAggSl1l2(boolean aggSl1l2) {
    _boolAggSl1l2 = aggSl1l2;
  }

  public boolean getAggGrad() {
    return _boolAggGrad;
  }

  public void setAggGrad(boolean aggGrad) {
    _boolAggGrad = aggGrad;
  }

  public boolean getAggSal1l2() {
    return _boolAggSal1l2;
  }

  public void setAggSal1l2(boolean aggSal1l2) {
    _boolAggSal1l2 = aggSal1l2;
  }

  public boolean getAggPct() {
    return _boolAggPct;
  }

  public void setAggPct(boolean aggPct) {
    _boolAggPct = aggPct;
  }

  public boolean getAggNbrCnt() {
    return _boolAggNbrCnt;
  }

  public void setAggNbrCnt(boolean aggNbrCnt) {
    _boolAggNbrCnt = aggNbrCnt;
  }

  public boolean getAggSsvar() {
    return _boolAggSsvar;
  }

  public void setAggSsvar(boolean aggSsvar) {
    _boolAggSsvar = aggSsvar;
  }

  public boolean getAggEcnt() {
    return _boolAggEcnt;
  }

  public void setAggEcnt(boolean aggEcnt) {
    _boolAggEcnt = aggEcnt;
  }

  public boolean getAggRps() {
    return _boolAggRps;
  }

  public void setAggRps(boolean aggRps) {
    _boolAggRps = aggRps;
  }

  public String getAggBootRepl() {
    return _strAggBootRepl;
  }

  public void setAggBootRepl(String aggBootRepl) {
    _strAggBootRepl = aggBootRepl;
  }

  public String getAggBootRandomSeed() {
    return _strAggBootRandomSeed;
  }

  public void setAggBootRandomSeed(String aggBootRandomSeed) {
    if (aggBootRandomSeed.trim().length() == 0) {
      _strAggBootRandomSeed = "NA";
    } else {
      try {
        Integer.valueOf(aggBootRandomSeed.trim());
        _strAggBootRandomSeed = aggBootRandomSeed;
      } catch (NumberFormatException e) {
        _strAggBootRandomSeed = "NA";
      }

    }
  }

  public Float getCl_step() {
    return cl_step;
  }

  public void setCl_step(String cl_step) {
    if (cl_step.trim().length() == 0) {
      this.cl_step = 0.05F;
    } else {
      try {
        this.cl_step = Float.valueOf(cl_step.trim());
      } catch (NumberFormatException e) {
        this.cl_step = 0.05F;
      }
    }
  }

  public String getAggBootCI() {
    return _strAggBootCI;
  }

  public void setAggBootCI(String aggBootCI) {
    _strAggBootCI = aggBootCI;
  }

  public boolean getEveqDis() {
    return _boolEveqDis;
  }

  public void setEveqDis(boolean eveqDis) {
    _boolEveqDis = eveqDis;
  }

  public boolean getCalcCtc() {
    return _boolCalcCtc;
  }

  public void setCalcCtc(boolean calcCtc) {
    _boolCalcCtc = calcCtc;
  }

  public boolean getCalcSl1l2() {
    return _boolCalcSl1l2;
  }

  public void setCalcSl1l2(boolean calcSl1l2) {
    _boolCalcSl1l2 = calcSl1l2;
  }

  public boolean getCalcGrad() {
    return _boolCalcGrad;
  }

  public void setCalcGrad(boolean calcGrad) {
    _boolCalcGrad = calcGrad;
  }

  public boolean getCalcSal1l2() {
    return _boolCalcSal1l2;
  }

  public void setCalcSal1l2(boolean calcSal1l2) {
    _boolCalcSal1l2 = calcSal1l2;
  }

  public boolean getCalcVal1l2() {
    return _boolCalcVal1l2;
  }

  public void setCalcVal1l2(boolean calcVal1l2) {
    _boolCalcVal1l2 = calcVal1l2;
  }

  public boolean getCalcVl1l2() {
    return _boolCalcVl1l2;
  }

  public void setCalcVl1l2(boolean calcVl1l2) {
    _boolCalcVl1l2 = calcVl1l2;
  }

  public boolean getAggVl1l2() {
    return _boolAggVl1l2;
  }

  public void setAggVl1l2(boolean aggVl1l2) {
    _boolAggVl1l2 = aggVl1l2;
  }

  public boolean getAggVal1l2() {
    return _boolAggVal1l2;
  }

  public void setAggVal1l2(boolean aggVal1l2) {
    _boolAggVal1l2 = aggVal1l2;
  }

  public boolean getRocPct() {
    return _boolRocPct;
  }

  public void setRocPct(boolean rocPct) {
    _boolRocPct = rocPct;
  }

  public boolean getTaylorVoc() {
    return _boolTaylorVoc;
  }

  public void setTaylorVoc(boolean taylorVoc) {
    _boolTaylorVoc = taylorVoc;
  }

  public boolean getTaylorShowGamma() {
    return _boolTaylorShowGamma;
  }

  public void setTaylorShowGamma(boolean taylorShowGamma) {
    _boolTaylorShowGamma = taylorShowGamma;
  }

  public boolean getRocCtc() {
    return _boolRocCtc;
  }

  public void setRocCtc(boolean rocCtc) {
    _boolRocCtc = rocCtc;
  }

  public String getPlotCI() {
    return _strPlotCI;
  }

  public void setPlotCI(String plotCI) {
    _strPlotCI = plotCI;
  }

  public String getPlotDisp() {
    return _strPlotDisp;
  }

  public void setPlotDisp(String plotDisp) {
    _strPlotDisp = plotDisp;
  }

  public String getShowSignif() {
    if (_strShowSignif.length() == 0) {
      _strShowSignif = _strPlotDisp.replace("TRUE", "FALSE");
    }
    return _strShowSignif;
  }

  public void setShowSignif(String showSignif) {
    _strShowSignif = showSignif;
  }

  public String getColors() {
    return _strColors;
  }

  public void setColors(String colors) {
    _strColors = colors;
  }

  public String getPch() {
    return _strPch;
  }

  public void setPch(String pch) {
    _strPch = pch;
  }

  public String getType() {
    return _strType;
  }

  public void setType(String type) {
    _strType = type;
  }

  public String getLty() {
    return _strLty;
  }

  public void setLty(String lty) {
    _strLty = lty;
  }

  public String getLwd() {
    return _strLwd;
  }

  public void setLwd(String lwd) {
    _strLwd = lwd;
  }

  public String getConSeries() {
    return _strConSeries;
  }

  public void setConSeries(String conSeries) {
    _strConSeries = conSeries;
  }

  public String getLegend() {
    return _strLegend;
  }

  public void setLegend(String legend) {
    _strLegend = legend;
  }

  public String getY1Lim() {
    return _strY1Lim;
  }

  public String getX1Lim() {
    return _strX1Lim;
  }

  public String getX1LimR() {
    String stripStr = _strX1Lim.replaceAll("^c\\(", "").replaceAll("\\)$", "");
    if (stripStr.isEmpty()) {
      return _strX1Lim;
    }
    String[] arr = stripStr.split(",");
    if (arr.length != 2) {
      return _strX1Lim;
    }
    int indMin = -1;
    int indMax = -1;
    arr[0] = arr[0].trim();
    arr[1] = arr[1].trim();
    for (int i=0; i< indyVal.length; i++){
      if(indyVal[i].equals(arr[0])){
        indMin = i;
      }
      if(indyVal[i].equals(arr[1])){
        indMax = i;
      }
      if(indMin != -1 && indMax != -1){
        break;
      }
    }
    return "c("+ indMin + "," + indMax + ")";
  }

  public void setY1Lim(String y1Lim) {
    _strY1Lim = y1Lim;
  }

  public void setX1Lim(String x1Lim) {
    _strX1Lim = x1Lim;
  }

  public String getY1Bufr() {
    return _strY1Bufr;
  }

  public void setY1Bufr(String y1Bufr) {
    _strY1Bufr = y1Bufr;
  }

  public String getY2Lim() {
    return _strY2Lim;
  }

  public void setY2Lim(String y2Lim) {
    _strY2Lim = y2Lim;
  }

  public String getY2Bufr() {
    return _strY2Bufr;
  }

  public void setY2Bufr(String y2Bufr) {
    _strY2Bufr = y2Bufr;
  }

  public boolean getVarianceInflationFactor() {
    return _boolVarianceInflationFactor;
  }

  public void setVarianceInflationFactor(boolean varianceInflationFactor) {
    this._boolVarianceInflationFactor = varianceInflationFactor;
  }

  public boolean getRevisionAc() {
    return revisionAc;
  }

  public void setRevisionAc(boolean revisionAc) {
    this.revisionAc = revisionAc;
  }

  public boolean getRevisionRun() {
    return revisionRun;
  }

  public void setRevisionRun(boolean revisionRun) {
    this.revisionRun = revisionRun;
  }

  public boolean getRevisionStat() {
    return revisionStat;
  }

  public void setRevisionStat(boolean revisionStat) {
    this.revisionStat = revisionStat;
  }

  public boolean getNormalizedHistogram() {
    return _boolNormalizedHistogram;
  }

  public void setNormalizedHistogram(boolean _boolNormalizedHistogram) {
    this._boolNormalizedHistogram = _boolNormalizedHistogram;
  }

  public boolean getCacheAggStat() {
    return _boolCacheAggStat;
  }

  public void setCacheAggStat(boolean _boolCacheAggStat) {
    this._boolCacheAggStat = _boolCacheAggStat;
  }

  public String getPlotStat() {
    return _plotStat;
  }

  public void setPlotStat(String _plotStat) {
    this._plotStat = _plotStat;
  }


  public boolean getKeepRevisions() {
    return keepRevisions;
  }

  public void setKeepRevisions(boolean keepRevisions) {
    this.keepRevisions = keepRevisions;
  }

  public String getOrderSeries() {
    if (_strOrderSeries.length() > 0) {
      return _strOrderSeries;
    } else {
      //c(20, 20, 20)
      String templ = getPch();
      int size = templ.split(",").length;
      String result = "c(";
      for (int i = 1; i <= size; i++) {
        result = result + i + ",";
      }
      return result.substring(0, result.length() - 1) + ")";
    }
  }

  public void setOrderSeries(String orderSeries) {
    _strOrderSeries = orderSeries;
  }

  public String getDiffSeries1() {
    return _strDiffSeries1;
  }

  public void setDiffSeries1(String _strDiffSeries1) {
    this._strDiffSeries1 = _strDiffSeries1;
  }

  public int getDiffSeries1Count() {
    if (_strDiffSeries1.equals("list()")) {
      return 0;
    }
    String[] diffSeries = _strDiffSeries1.split("c\\(");
    return diffSeries.length - 1;
  }

  public int getDiffSeries2Count() {
    if (_strDiffSeries2.equals("list()")) {
      return 0;
    }
    String[] diffSeries = _strDiffSeries2.split("c\\(");
    return diffSeries.length - 1;
  }

  public String getDiffSeries2() {
    return _strDiffSeries2;
  }

  public void setDiffSeries2(String _strDiffSeries2) {
    this._strDiffSeries2 = _strDiffSeries2;
  }

  public boolean getEqualizeByIndep() {
    return this._equalizeByIndep;
  }

  public void setEqualizeByIndep(Boolean equalizeByIndep) {
    this._equalizeByIndep = equalizeByIndep;
  }

  public void addSummaryCurve(String value) {
    this.summaryCurve.add(value);
  }

  public List<String> getSummaryCurve() {
    return this.summaryCurve;
  }

  public boolean getAddPointThresholds() {
    return addPointThresholds;
  }

  public void setAddPointThresholds(boolean addPointThresholds) {
    this.addPointThresholds = addPointThresholds;
  }

  public boolean getAddReferenceLine() {
    return addReferenceLine;
  }

  public void setAddReferenceLine(boolean addReferenceLine) {
    this.addReferenceLine = addReferenceLine;
  }

  public boolean getCreateHtml() {
    return createHtml;
  }

  public void setCreateHtml(boolean createHtml) {
    this.createHtml = createHtml;
  }

  public boolean getAddSkillLine() {
    return addSkillLine;
  }

  public void setAddSkillLine(boolean addSkillLine) {
    this.addSkillLine = addSkillLine;
  }

  public boolean getInsetHist() {
    return insetHist;
  }

  public void setInsetHist(boolean insetHist) {
    this.insetHist = insetHist;
  }

  public boolean getAddNoSkillLine() {
    return addNoSkillLine;
  }

  public void setAddNoSkillLine(boolean addNoSkillLine) {
    this.addNoSkillLine = addNoSkillLine;
  }

  public String getSummaryCurveRformat() {
    String result = "c(";
    for (String stat : summaryCurve) {
      result = result + "'" + stat + "'" + ",";
    }
    if (!summaryCurve.isEmpty()) {
      result = result.substring(0, result.length() - 1);
    }
    return result + ")";
  }


  public List<String> getCurrentDBName() {
    return currentDBName;
  }

  public void setCurrentDBName(List<String> currentDBName) {
    this.currentDBName = currentDBName;
  }

  /**
   * Examine the first statistic in the first <dep1> structure to determine if the job is MODE ratio
   * statistics
   *
   * @return true if the checked stat is a MODE stat, false otherwise
   */
  public boolean isModeRatioJob() {
    if (isModeRatio == null) {
      MVOrderedMap[] listDep = getDepGroups();
      if (listDep.length > 0) {
        String[][] listFcstVarStat = MVUtil.buildFcstVarStatList((MVOrderedMap) listDep[0]
                .get("dep1"));
        isModeRatio = listFcstVarStat.length > 0
                && MVUtil.modeRatioField.contains(listFcstVarStat[0][1]);
      } else {
        isModeRatio = false;
      }
    }
    return isModeRatio;
  }

  /**
   * Examine the first statistic in the first <dep1> structure to determine if the job is MODE ratio
   * statistics
   *
   * @return true if the checked stat is a MODE stat, false otherwise
   */
  public boolean isMtdRatioJob() {
    if (isMtdRatio == null) {
      MVOrderedMap[] listDep = getDepGroups();
      if (listDep.length > 0) {
        String[][] listFcstVarStat = MVUtil.buildFcstVarStatList((MVOrderedMap) listDep[0]
                .get("dep1"));
        isMtdRatio = listFcstVarStat.length > 0
                && MVUtil.mtdRatioField.contains(listFcstVarStat[0][1]);
      } else {
        isMtdRatio = false;
      }
    }
    return isMtdRatio;
  }

  /**
   * Examine the first statistic in the first <dep1> structure to determine if the job is plotting
   * stat statistics or MODE statisticss
   *
   * @return true if the checked stat is a MODE stat, false otherwise
   */
  public boolean isModeJob() {
    if (isMode == null) {
      MVOrderedMap[] listDep = getDepGroups();
      if (listDep.length > 0) {
        String[][] listFcstVarStat = MVUtil.buildFcstVarStatList(
                (MVOrderedMap) listDep[0].get("dep1"));
        String strStat = MVUtil.parseModeStat(listFcstVarStat[0][1])[0];

        isMode = MVUtil.modeSingleStatField.containsKey(strStat)
                || MVUtil.modePairStatField.containsKey(strStat)
                || MVUtil.modeRatioField.contains(listFcstVarStat[0][1]);
      } else {
        isMode = false;
      }
    }
    return isMode;
  }

  public boolean isMtdJob() {
    if (isMtd == null) {
      MVOrderedMap[] listDep = getDepGroups();
      if (listDep.length > 0) {
        String[][] listFcstVarStat = MVUtil.buildFcstVarStatList((MVOrderedMap) listDep[0]
                .get("dep1"));

        String[] listStatComp = listFcstVarStat[0][1].split("_");
        String stat = listFcstVarStat[0][1]
                .replace("_" + listStatComp[listStatComp.length - 1], "");

        isMtd = MVUtil.mtd3dSingleStatField.containsKey(stat)
                || MVUtil.mtd3dPairStatField.containsKey(stat)
                || MVUtil.mtd2dStatField.containsKey(stat)
                || MVUtil.mtdRatioField.contains(listFcstVarStat[0][1]);
      } else {
        isMtd = false;
      }
    }
    return isMtd;
  }

  private void validateSQL(String str) throws DatabaseException {
    if (str.toLowerCase().contains(" and ") && !str.toLowerCase().contains(" between ")) {
      throw new DatabaseException("String " + str + " includes SQL unsafe word AND or BETWEEN");
    }
    for (String noSQL : DatabaseManager.SQL_INJECTION_WORDS) {
      if (str.contains(noSQL.toLowerCase()) || str.contains(noSQL.toUpperCase())) {
        throw new DatabaseException("String " + str + " includes SQL unsafe word " + noSQL);
      }
    }
  }

  public Integer getContourIntervals() {
    return contourIntervals;
  }

  public void setContourIntervals(String contourIntervals) throws ValidationException {
    try {
      this.contourIntervals = Integer.valueOf(contourIntervals);
    } catch (NumberFormatException e) {
      //do noting - use the default value
    }
  }

  public String getColorPalette() {
    return colorPalette;
  }

  public void setColorPalette(String colorPalette) {
    this.colorPalette = colorPalette;
  }


  public String getExecutionType() {
    return executionType;
  }

  public void setExecutionType(String executionType) {
    if (executionType.equals(RSCRIPT) || executionType.equals(PYTHON)) {
      this.executionType = executionType;
    }
  }

  public String getAnnotationTemplate() {
    return annotationTemplate;
  }

  public void setAnnotationTemplate(String annotationTemplate) {
    this.annotationTemplate = annotationTemplate;
  }

  public Boolean getAddColorBar() {
    return isAddColorBar;
  }

  public void setAddColorBar(Boolean addColorBar) {
    isAddColorBar = addColorBar;
  }

  public Boolean getAddContourOverlay() {
    return isAddContourOverlay;
  }

  public void setAddContourOverlay(Boolean addContourOverlay) {
    isAddContourOverlay = addContourOverlay;
  }

  public Boolean getCircularBlockBootstrap() {
    return isCircularBlockBootstrap;
  }

  public void setCircularBlockBootstrap(Boolean addCircularBlockBootstrap) {
    isCircularBlockBootstrap = addCircularBlockBootstrap;
  }

  public Boolean getReverseX() {
    return isReverseX;
  }

  public void setReverseX(Boolean reverseX) {
    isReverseX = reverseX;
  }

  public Boolean getReverseY() {
    return isReverseY;
  }

  public void setReverseY(Boolean reverseY) {
    isReverseY = reverseY;
  }

  public boolean isAggStat() {
    return this.getAggCtc()
            || this.getAggSl1l2()
            || this.getAggSal1l2()
            || this.getAggNbrCnt()
            || this.getAggSsvar()
            || this.getAggVl1l2()
            || this.getAggVal1l2()
            || this.getAggGrad()
            || this.getAggPct()
            || this.getAggEcnt()
            || this.getAggNbrCtc()
            || this.getAggRps();

  }

  public boolean isCalcStat() {
    return this.getCalcCtc()
            || this.getCalcSl1l2()
            || this.getCalcSal1l2()
            || this.getCalcVl1l2()
            || this.getCalcVal1l2()
            || this.getCalcGrad();
  }


  public boolean getAggNbrCtc() {
    return _boolAggNbrCtc;
  }

  public void setAggNbrCtc(boolean  nbrCtc) {
     _boolAggNbrCtc = nbrCtc;
  }
  public String getLineType(){
    if(this.getAggCtc()){
      return "ctc";
    }
    if(this.getAggSl1l2()){
      return "sl1l2";
    }
    if(this.getAggSal1l2()){
      return "sal1l2";
    }
    if(this.getAggNbrCnt()){
      return "nbr_cnt";
    }
    if(this.getAggNbrCtc()){
      return "nbr_ctc";
    }
    if(this.getAggSsvar()){
      return "ssvar";
    }
    if(this.getAggVl1l2()){
      return "vl1l2";
    }
    if(this.getAggVal1l2()){
      return "val1l2";
    }
    if(this.getAggGrad()){
      return "grad";
    }
    if(this.getAggPct()){
      return "pct";
    }
    if(this.getAggEcnt()){
      return "ecnt";
    }
    if(this.getAggRps()){
      return "rps";
    }
    if(this.getCalcCtc()){
      return "ctc";
    }
    if(this.getCalcGrad()){
      return "grad";
    }
    if(this.getCalcSal1l2()){
      return "sal1l2";
    }
    if(this.getCalcSl1l2()){
      return "sl1l2";
    }
    if(this.getCalcVal1l2()){
      return "val1l2";
    }
    if(this.getCalcVl1l2()){
      return "vl1l2";
    }
    return "N/A";
  }
}

