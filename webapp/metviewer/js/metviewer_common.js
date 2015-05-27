var value_to_desc_map = {};
value_to_desc_map['BAGSS'] = 'Bias-Corrected Gilbert Skill Score';
value_to_desc_map['AFSS'] = 'Asymptotic Fractions Skill Score';
value_to_desc_map['UFSS'] = 'Uniform Fractions Skill Score';
value_to_desc_map['F_RATE'] = 'Forecast Rate';
value_to_desc_map['O_RATE'] = 'Observation Rate';
value_to_desc_map['IQR'] = 'Inter-quartile range';
value_to_desc_map['MAD'] = 'Median Absolute Deviation';
value_to_desc_map['LODDS'] = 'Log Odds Ratio';
value_to_desc_map['ORSS'] = 'Odds Ratio Skill Score';
value_to_desc_map['EDS'] = 'Extreme Dependency Score';
value_to_desc_map['SEDS'] = 'Symmetric Extreme Dependency Score';
value_to_desc_map['EDI'] = 'Extreme Dependency Index';
value_to_desc_map['SEDI'] = 'Symmetric Extreme Dependency Index';
value_to_desc_map['solid'] ='1';
value_to_desc_map['dashed'] ='2';
value_to_desc_map['dotted'] ='3';
value_to_desc_map['dot-dash']='4';
value_to_desc_map['long dash']='5';
value_to_desc_map['long short']='6';
value_to_desc_map['normal'] ='1';
value_to_desc_map['bold'] ='2';
value_to_desc_map['italic'] ='3';
value_to_desc_map['bold italic']='4';
value_to_desc_map['greek'] ='5';
value_to_desc_map['ACPC_03'] = '3hr accumulation';
value_to_desc_map['ACPC_06'] = '6hr accumulation';
value_to_desc_map['ACPC_24'] = '24hr accumulation';
value_to_desc_map['REFC'] = 'Reflectivity';
value_to_desc_map['DPT'] = 'Dew point temperature';
value_to_desc_map['HGT'] = 'Height';
value_to_desc_map['TMP'] = 'Temperature';
value_to_desc_map['PMSL'] = 'Mean sea level pressure';
value_to_desc_map['UGRD'] = 'U-Wind';
value_to_desc_map['VGRD'] = 'V-Wind';
value_to_desc_map['UGRD_VGRD'] = 'U and V-Wind';
value_to_desc_map['WIND'] = 'Wind';
value_to_desc_map['SPFH'] = 'Specific humidity';
value_to_desc_map['PWAT'] = 'Precipital water';
value_to_desc_map['ACC'] = 'Accuracy';
value_to_desc_map['BASER'] = 'Base rate, aka Observed relative frequency';
value_to_desc_map['BCMSE'] = 'Bias-corrected mean squared error';
value_to_desc_map['BCRMSE'] = 'Bias-corrected root mean square error';
value_to_desc_map['CSI'] = 'Critical success index, aka Threat score';
value_to_desc_map['E10'] = '10th percentile of the error';
value_to_desc_map['E25'] = '25th percentile of the error';
value_to_desc_map['E50'] = '50th percentile of the error';
value_to_desc_map['E75'] = '75th percentile of the error';
value_to_desc_map['E90'] = '90th percentile of the error';
value_to_desc_map['ESTDEV'] = 'Standard deviation of the error';
value_to_desc_map['FAR'] = 'False alarm ratio';
value_to_desc_map['FBAR'] = 'Forecast mean';
value_to_desc_map['FBIAS'] = 'Bias, aka Frequency bias';
value_to_desc_map['FMEAN'] = 'Forecast mean';
value_to_desc_map['FSTDEV'] = 'Forecast standard deviation';
value_to_desc_map['GSS'] = 'Gilbert skill score, aka Equitable threat score';
value_to_desc_map['HK'] = 'Hanssen Kuipers Discriminant';
value_to_desc_map['HSS'] = 'Heidke skill score';
value_to_desc_map['KT_CORR'] = 'Kendall Tau Rank Correlation Coefficient';
value_to_desc_map['MAE'] = 'Mean absolute error';
value_to_desc_map['MBIAS'] = 'Multiplicative Bias';
value_to_desc_map['ME'] = 'Mean error, aka Additive bias';
value_to_desc_map['MSE'] = 'Mean squared error';
value_to_desc_map['OBAR'] = 'Observation Mean';
value_to_desc_map['ODDS'] = 'Odds Ratio';
value_to_desc_map['OSTDEV'] = 'Observation Standard Deviation';
value_to_desc_map['PODN'] = 'Probability of Detecting No';
value_to_desc_map['PODY'] = 'Probability of Detecting Yes';
value_to_desc_map['POFD'] = 'Probability of false detection';
value_to_desc_map['PR_CORR'] = 'Pearson\'s Correlation Coefficient';
value_to_desc_map['RMSE'] = 'Root-mean squared error';
value_to_desc_map['SP_CORR'] = 'Spearman\'s Rank Correlation Coefficient';
value_to_desc_map['MCTS_ACC'] = 'Multi-category: Accuracy';
value_to_desc_map['MCTS_GER'] = 'Multi-category: Gerrity score';
value_to_desc_map['MCTS_HK'] = 'Multi-category: Hanssen Kuipers Discrim';
value_to_desc_map['MCTS_HSS'] = 'Multi-category: Heidke skill score';
value_to_desc_map['NBR_ACC'] = 'Nbrhd methods: Accuracy';
value_to_desc_map['NBR_BASER']= 'Nbrhd base rate';
value_to_desc_map['NBR_CSI'] = 'Nbrhd critical success index';
value_to_desc_map['NBR_FAR'] = 'Nbrhd false alarm rate';
value_to_desc_map['NBR_FBIAS']= 'Nbrhd frequesncy bias';
value_to_desc_map['NBR_FBS'] = 'Nbrhd methods cont: Fract brier score';
value_to_desc_map['NBR_FMEAN']= 'Nbrhd forecast mean';
value_to_desc_map['NBR_FSS'] = 'Nbrhd methods cont: Fract skill score';
value_to_desc_map['NBR_GSS'] = 'Nbrhd Gilbert skill score';
value_to_desc_map['NBR_HK'] = 'Nbrhd Hanssen Kuipers discrim';
value_to_desc_map['NBR_HSS'] = 'Nbrhd Heidke skill score';
value_to_desc_map['NBR_ODDS'] = 'Nbrhd odds ratio';
value_to_desc_map['NBR_PODN'] = 'Nbrhd prob of detecting no';
value_to_desc_map['NBR_PODY'] = 'Nbrhd prob of detecting yes';
value_to_desc_map['NBR_POFD'] = 'Nbrhd prob of false detecting';
value_to_desc_map['ACOV'] = 'Sum of area divided by total';
value_to_desc_map['CNT'] = 'Count of object_id';
value_to_desc_map['CNTSUM'] = 'Count of object_id';
value_to_desc_map['CENTX'] = 'Centroid X in grid-units';
value_to_desc_map['CENTY'] = 'Centroid Y in grid-units';
value_to_desc_map['CENTLAT']= 'Centroid lat';
value_to_desc_map['CENTLON']= 'Centroid lon';
value_to_desc_map['AXAVG'] = 'Axis average';
value_to_desc_map['LEN'] = 'Length';
value_to_desc_map['WID'] = 'Width';
value_to_desc_map['ASPECT'] = 'Aspect';
value_to_desc_map['AREA'] = 'Area';
value_to_desc_map['AREAFIL']= 'Area filter';
value_to_desc_map['AREATHR']= 'Area threshold';
value_to_desc_map['CURV'] = 'Curvature';
value_to_desc_map['CURVX'] = 'Curvature X';
value_to_desc_map['CURVY'] = 'Curvature Y';
value_to_desc_map['CPLX'] = 'Complexity';
value_to_desc_map['INT10'] = 'Intensity 10th percentile';
value_to_desc_map['INT25'] = 'Intensity 25th percentile';
value_to_desc_map['INT50'] = 'Intensity 50th percentile';
value_to_desc_map['INT75'] = 'Intensity 75th percentile';
value_to_desc_map['INT90'] = 'Intensity 90th percentile';
value_to_desc_map['INTN'] = 'Intensity designed for use in PERCINTRATIO';
value_to_desc_map['INTSUM'] = 'Intensity sum';
value_to_desc_map['RATIO_FSA_ASA'] = '% of simple objects that are forecast';
value_to_desc_map['RATIO_OSA_ASA'] = '% of simple objects that are observation';
value_to_desc_map['RATIO_ASM_ASA'] = '% of simple objects that are matched';
value_to_desc_map['RATIO_ASU_ASA'] = '% of simple objects that are unmatched';
value_to_desc_map['RATIO_FSM_FSA'] = '% of simple forecast objects that are matched';
value_to_desc_map['RATIO_FSU_FSA'] = '% of simple forecast objects that are unmatched';
value_to_desc_map['RATIO_OSM_OSA'] = '% of simple simple observation objects that are matched';
value_to_desc_map['RATIO_OSU_OSA'] = '% of simple simple observation objects that are unmatched';
value_to_desc_map['RATIO_FSM_ASM'] = '% of simple matched objects that are forecasts';
value_to_desc_map['RATIO_OSM_ASM'] = '% of simple matched objects that are observations';
value_to_desc_map['RATIO_FSU_ASU'] = '% of simple unmatched objects that are forecast';
value_to_desc_map['RATIO_OSU_ASU'] = '% of simple unmatched objects that are observation';
value_to_desc_map['RATIO_ASA_AAA'] = '% of all objects that are simple';
value_to_desc_map['RATIO_ACA_AAA'] = '% of all objects that are cluster';
value_to_desc_map['RATIO_FSA_AAA'] = 'What is';
value_to_desc_map['RATIO_OSA_AAA'] = 'What is';
value_to_desc_map['RATIO_FSA_FAA'] = '% of all forecast objects that are simple';
value_to_desc_map['RATIO_FCA_FAA'] = '% of all forecast objects that are cluster';
value_to_desc_map['RATIO_OSA_OAA'] = '% of all observation objects that are simple';
value_to_desc_map['RATIO_OCA_OAA'] = '% of all observation objects that are cluster';
value_to_desc_map['RATIO_FCA_ACA'] = '% of cluster objects that are forecast';
value_to_desc_map['RATIO_OCA_ACA'] = '% of cluster objects that are observation';
value_to_desc_map['RATIO_FSA_OSA'] = 'Ratio of simple forecasts to simple observations [frequency bias]';
value_to_desc_map['RATIO_OSA_FSA'] = 'Ratio of simple observations to simple forecasts [1 / frequency bias]';
value_to_desc_map['RATIO_ACA_ASA'] = 'Ratio of cluster objects to simple objects';
value_to_desc_map['RATIO_ASA_ACA'] = 'Ratio of simple objects to cluster objects';
value_to_desc_map['RATIO_FCA_FSA'] = 'Ratio of cluster forecast objects to simple forecast objects';
value_to_desc_map['RATIO_FSA_FCA'] = 'Ratio of simple forecast objects to cluster forecast objects';
value_to_desc_map['RATIO_OCA_OSA'] = 'Ratio of cluster observation objects to simple observation objects';
value_to_desc_map['RATIO_OSA_OCA'] = 'Ratio of simple observation objects to cluster observation objects';
value_to_desc_map['OBJHITS'] = 'Hits =/2';
value_to_desc_map['OBJMISSES']= 'Misses = OSU';
value_to_desc_map['OBJFAS'] = 'False Alarms = FSU';
value_to_desc_map['OBJCSI'] = 'CSI = hits //2 + OSU + FSU]';
value_to_desc_map['OBJPODY'] = 'PODY = hits //2 + OSU]';
value_to_desc_map['OBJFAR'] = 'FAR = false alarms //2 + FSU]';
value_to_desc_map['OBJFBIAS'] = 'FBIAS = RATIO_FSA_OSA = FSA / OSA';
value_to_desc_map['AREARAT_FSA_ASA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSA_ASA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_ASM_ASA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_ASU_ASA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSM_FSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSU_FSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSM_OSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSU_OSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSM_ASM'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSM_ASM'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSU_ASU'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSU_ASU'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSA_AAA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSA_AAA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSA_FAA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FCA_FAA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSA_OAA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OCA_OAA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FCA_ACA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OCA_ACA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSA_OSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSA_FSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_ACA_ASA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_ASA_ACA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FCA_FSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_FSA_FCA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OCA_OSA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['AREARAT_OSA_OCA'] = 'Area-weighted % of simple objects forecasted';
value_to_desc_map['CENTDIST'] = 'Centroid distance';
value_to_desc_map['BOUNDDIST'] = 'Boundry distance';
value_to_desc_map['HULLDIST'] = 'Convex hull distance';
value_to_desc_map['ANGLEDIFF'] = 'Angle difference';
value_to_desc_map['AREARATIO'] = 'Area ratio';
value_to_desc_map['INTAREA'] = 'Intersection area';
value_to_desc_map['UNIONAREA'] = 'Union area';
value_to_desc_map['SYMDIFF'] = 'Symmetric difference or non-intersecting area';
value_to_desc_map['INTOVERAREA'] = 'Ratio of intersection area to union area';
value_to_desc_map['CMPLXRATIO'] = 'Complexity ratio';
value_to_desc_map['PERCINTRATIO']= 'Ratio of intensity at percentile defined by INTNN';
value_to_desc_map['INT'] = 'Interest';
value_to_desc_map['MAXINT'] = 'Maximun interest';
value_to_desc_map['MAXINTF'] = 'Maximun interest for forecast objects';
value_to_desc_map['MAXINTO'] = 'Maximun interest for observation objects';

var listStatModelRatio = [ "RATIO_FSA_ASA", "RATIO_OSA_ASA", "RATIO_ASM_ASA", "RATIO_ASU_ASA", "RATIO_FSM_FSA",
    "RATIO_FSU_FSA", "RATIO_OSM_OSA", "RATIO_OSU_OSA", "RATIO_FSM_ASM", "RATIO_OSM_ASM",
    "RATIO_FSU_ASU", "RATIO_OSU_ASU", "RATIO_FSA_AAA", "RATIO_OSA_AAA", "RATIO_FSA_FAA",
    "RATIO_FCA_FAA", "RATIO_OSA_OAA", "RATIO_OCA_OAA", "RATIO_FCA_ACA", "RATIO_OCA_ACA",
    "RATIO_FSA_OSA", "RATIO_OSA_FSA", "RATIO_ACA_ASA", "RATIO_ASA_ACA", "RATIO_FCA_FSA",
    "RATIO_FSA_FCA", "RATIO_OCA_OSA", "RATIO_OSA_OCA", "OBJHITS", "OBJMISSES", "OBJFAS",
    "OBJCSI", "OBJPODY", "OBJFAR",

    "AREARAT_FSA_ASA", "AREARAT_OSA_ASA", "AREARAT_ASM_ASA", "AREARAT_ASU_ASA", "AREARAT_FSM_FSA",
    "AREARAT_FSU_FSA", "AREARAT_OSM_OSA", "AREARAT_OSU_OSA", "AREARAT_FSM_ASM", "AREARAT_OSM_ASM",
    "AREARAT_FSU_ASU", "AREARAT_OSU_ASU", "AREARAT_FSA_AAA", "AREARAT_OSA_AAA", "AREARAT_FSA_FAA",
    "AREARAT_FCA_FAA", "AREARAT_OSA_OAA", "AREARAT_OCA_OAA", "AREARAT_FCA_ACA", "AREARAT_OCA_ACA",
    "AREARAT_FSA_OSA", "AREARAT_OSA_FSA", "AREARAT_ACA_ASA", "AREARAT_ASA_ACA", "AREARAT_FCA_FSA",
    "AREARAT_FSA_FCA", "AREARAT_OCA_OSA", "AREARAT_OSA_OCA", "OBJAHITS", "OBJAMISSES", "OBJAFAS",
    "OBJACSI", "OBJAPODY", "OBJAFAR"
];
var listStatModeSingle = [
    "ACOV", "CNT", "CNTSUM", "CENTX", "CENTY", "CENTLAT", "CENTLON", "AXAVG", "LEN", "WID", "ASPECT",
    "AREA", "AREAFIL", "AREATHR", "CURV", "CURVX", "CURVY", "CPLX", "INT10", "INT25", "INT50",
    "INT75", "INT90", "INTN", "INTSUM"
];

var listStatModePair = [
    "CENTDIST", "BOUNDDIST", "HULLDIST", "ANGLEDIFF", "AREARATIO", "INTAREA", "UNIONAREA",
    "SYMDIFF", "INTOVERAREA", "CMPLXRATIO", "PERCINTRATIO", "INT", "MAXINT", "MAXINTF", "MAXINTO"
];
var listStatMode = listStatModeSingle.concat(listStatModePair);

var series_var_value_to_title_stat_map={};
series_var_value_to_title_stat_map['model'] = 'MODEL';
series_var_value_to_title_stat_map['fcst_lead'] = 'FCST_LEAD';
series_var_value_to_title_stat_map['fcst_valid_beg'] = 'FCST_VALID_BEG';
series_var_value_to_title_stat_map['valid_hour'] = 'VALID_HOUR';
series_var_value_to_title_stat_map['fcst_init_beg'] = 'FCST_INIT_BEG';
series_var_value_to_title_stat_map['init_hour'] = 'INIT_HOUR';
series_var_value_to_title_stat_map['fcst_lev'] = 'FCST_LEV';
series_var_value_to_title_stat_map['obtype'] = 'OBTYPE';
series_var_value_to_title_stat_map['vx_mask'] = 'VX_MASK';
series_var_value_to_title_stat_map['interp_mthd'] = 'INTERP_MTHD';
series_var_value_to_title_stat_map['interp_pnts'] = 'INTERP_PNTS';
series_var_value_to_title_stat_map['fcst_thresh'] = 'FCST_THRESH';

var fix_var_value_to_title_stat_map = {};
fix_var_value_to_title_stat_map['fcst_lead'] = 'FCST_LEAD';
fix_var_value_to_title_stat_map['model'] = 'MODEL';
fix_var_value_to_title_stat_map['fcst_valid_beg'] = 'FCST_VALID_BEG';
fix_var_value_to_title_stat_map['valid_hour'] = 'VALID_HOUR';
fix_var_value_to_title_stat_map['fcst_init_beg'] = 'FCST_INIT_BEG';
fix_var_value_to_title_stat_map['init_hour'] = 'INIT_HOUR';
fix_var_value_to_title_stat_map['fcst_lev'] = 'FCST_LEV';
fix_var_value_to_title_stat_map['obtype'] = 'OBTYPE';
fix_var_value_to_title_stat_map['vx_mask'] = 'VX_MASK';
fix_var_value_to_title_stat_map['interp_mthd'] = 'INTERP_MTHD';
fix_var_value_to_title_stat_map['interp_pnts'] = 'INTERP_PNTS';
fix_var_value_to_title_stat_map['fcst_thresh'] = 'FCST_THRESH';
fix_var_value_to_title_stat_map['obs_thresh'] = 'OBS_THRESH';

var fix_var_value_to_title_mode_map = {};
fix_var_value_to_title_mode_map['fcst_lead'] = 'FCST_LEAD';
fix_var_value_to_title_mode_map['model'] = 'MODEL';
fix_var_value_to_title_mode_map['fcst_valid'] = 'FCST_VALID';
fix_var_value_to_title_mode_map['valid_hour'] = 'VALID_HOUR';
fix_var_value_to_title_mode_map['fcst_init'] = 'FCST_INIT';
fix_var_value_to_title_mode_map['init_hour'] = 'INIT_HOUR';
fix_var_value_to_title_mode_map['fcst_accum'] = 'FCST_ACCUM';
fix_var_value_to_title_mode_map['fcst_rad'] = 'FCST_RAD';
fix_var_value_to_title_mode_map['fcst_thr'] = 'FCST_THR';
fix_var_value_to_title_mode_map['fcst_lev'] = 'FCST_LEV';

var indy_var_value_to_title_stat_map = {};
indy_var_value_to_title_stat_map['fcst_lead'] = 'FCST_LEAD';
indy_var_value_to_title_stat_map['model'] = 'MODEL';
indy_var_value_to_title_stat_map['fcst_lev'] = 'FCST_LEV';
indy_var_value_to_title_stat_map['fcst_thresh'] = 'FCST_THRESH';
indy_var_value_to_title_stat_map['obs_thresh'] = 'OBS_THRESH';
indy_var_value_to_title_stat_map['fcst_valid_beg'] = 'FCST_VALID_BEG';
indy_var_value_to_title_stat_map['valid_hour'] = 'VALID_HOUR';
indy_var_value_to_title_stat_map['fcst_init_beg'] = 'FCST_INIT_BEG';
indy_var_value_to_title_stat_map['init_hour'] = 'INIT_HOUR';
indy_var_value_to_title_stat_map['interp_pnts'] = 'INTERP_PNTS';
indy_var_value_to_title_stat_map['vx_mask'] = 'VX_MASK';

var indy_var_value_to_title_mode_map = {};
indy_var_value_to_title_mode_map['fcst_lead'] = 'FCST_LEAD';
indy_var_value_to_title_mode_map['model'] = 'MODEL';
indy_var_value_to_title_mode_map['fcst_lev'] = 'FCST_LEV';
indy_var_value_to_title_mode_map['fcst_thr'] = 'FCST_THR';
indy_var_value_to_title_mode_map['fcst_valid'] = 'FCST_VALID';
indy_var_value_to_title_mode_map['valid_hour'] = 'VALID_HOUR';
indy_var_value_to_title_mode_map['fcst_init'] = 'FCST_INIT';
indy_var_value_to_title_mode_map['init_hour'] = 'INIT_HOUR';
indy_var_value_to_title_mode_map['fcst_rad'] = 'FCST_RAD';
indy_var_value_to_title_mode_map['vx_mask'] = 'VX_MASK';




var fcst_var_y1_indexes = [1];
var series_var_y1_indexes = [1];
var fcst_var_y2_indexes = [];
var series_var_y2_indexes = [];
var fixed_var_indexes = [];
var date_period_indexes = [1];
var firstSeriesFormatting= {order: "1", /*y_axis: "Y1",*/ hide: "No", /*title: "GSI3 TMP BASER",*/ plot_ci: "none", color: "", pch: 20, type: "b", lty: "1", lwd: "1", show_signif: "No", con_series: "1", legend: "", id:"1"};
var seriesDiffY1 = [];
var seriesDiffY2 = [];
var previousIndVarValResponse ;
var fixVarValResponse={};
var seriesY1VarValResponse={};
var seriesY2VarValResponse={};
var xml = '<?xml version="1.0"?><Request/>';
var group_name_to_value_map;
var indy_var_vals_to_attr;
var fcst_vars=[];
var fcst_vars_stats=[];

function cleanUp() {

    fcst_var_y1_indexes = [1];
    series_var_y1_indexes = [1];
    fcst_var_y2_indexes = [];
    series_var_y2_indexes = [];
    fixed_var_indexes = [];
    date_period_indexes = [1];
    seriesDiffY1 = [];
    seriesDiffY2 = [];
    previousIndVarValResponse = null;
    fixVarValResponse = {};
    seriesY1VarValResponse={};
    seriesY2VarValResponse={};
    group_name_to_value_map = null;
    fcst_vars=[];
    fcst_vars_stats=[];
}

/**
 * Determine if the input statistic is a MODE statistic and return true if so,
 * false otherwise.
 */
function isModeStat(stat) {
   if($.inArray(stat, listStatModelRatio) >= 0 ) {
       return true;
   }else{
       var statArr = stat.split("_");
       if(statArr.length == 2){
          if($.inArray(statArr[0], listStatMode) >= 0 || statArr[0] == 'ACOVACOV'){
              return true;
          }else{
              return false;
          }
       }else{
        return false;
       }
   }
}
function getForecastVariablesHist() {
    var selectedDatabase = $("#database").multiselect("getChecked").val();

    //<request><db_con>mv_dart_gsi</db_con><list_stat><id>0</id><stat_fcst_var>TMP</stat_fcst_var></list_stat></request>
    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        dataType: 'xml',
        processData: false,
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><stat_field>FCST_VAR</stat_field></list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {
        },
        success: function (data) {
            var values = $(data).find("val");
            for (var i = 0; i < values.length; i++) {
                fcst_vars.push( $(values[i]).text());
            }
        }
    });
    var fcst_var="TMP";
    if(fcst_vars.length > 0){
        fcst_var = fcst_vars[0];
    }

    $.ajax({
            async: false,
            url: "servlet",
            type: "POST",
            dataType: 'xml',
            processData: false,
            data: '<request><db_con>' + selectedDatabase + '</db_con><list_stat><id>0</id><stat_fcst_var>'+fcst_var+'</stat_fcst_var></list_stat></request>',
            error: function (jqXHR, textStatus, errorThrown) {
            },
            success: function (data) {
                var values = $(data).find("val");
                for (var i = 0; i < values.length; i++) {
                    fcst_vars_stats.push( $(values[i]).text());
                }
            }
        });
}

function updateForecastVariables() {
    $('#listdt').jqGrid('clearGridData');

    selected_mode = $("#plot_data").multiselect("getChecked").val();
    if(!selected_mode){
        selected_mode = 'stat';
    }

    var select_y1 = $("#fcst_var_y1_1");
    var select_y2 = $('#fcst_var_y2_1');
    var indy_var_val = $('#indy_var_val');

    //TO_DO reset all selects
    select_y1.empty();
    select_y1.multiselect('refresh');
    select_y2.empty();
    try{
        select_y2.multiselect('refresh');
    }catch (err){}
    indy_var_val.empty();
    indy_var_val.multiselect('refresh');
    for (var i = 1; i < fcst_var_y1_indexes.length; i++) {
        removeFcstVarSeriesBox("fcst_var_y1_" + fcst_var_y1_indexes[i]);
    }

    for (var i = 0; i < fcst_var_y2_indexes.length; i++) {
        removeFcstVarSeriesBox("fcst_var_y2_" + fcst_var_y2_indexes[i]);
    }

    for (var i = 1; i < series_var_y1_indexes.length; i++) {
        removeSeriesVarSeriesBox("series_var_y1_" + series_var_y1_indexes[i]);
    }

    for (var i = 0; i < series_var_y2_indexes.length; i++) {
        removeSeriesVarSeriesBox("series_var_y2_" + series_var_y2_indexes[i]);
    }
    var fixed_var_indexes_copy = fixed_var_indexes.slice();
    for (var i = 0; i < fixed_var_indexes_copy.length; i++) {
        removeFixedVarSeries("fixed_var_" + fixed_var_indexes_copy[i]);
    }
    $("#group_series_var_y1_1").prop('checked', false);


    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();

    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        dataType:'xml',
        processData:false,
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><'+ selected_mode + '_field>FCST_VAR</'+selected_mode +'_field></list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {
        },
        success: function (data) {
            var values = $(data).find("val");

            var opt;
            if (values.length > 0) {
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    opt = $('<option />', {
                        value: t.text(),
                        text: t.text(),
                        title: value_to_desc_map[t.text()]
                    });
                    opt.appendTo(select_y1);
                    opt.clone().appendTo(select_y2);
                }
                select_y1.multiselect('refresh');
                try{
                 select_y2.multiselect('refresh');
                }catch (err){}
            } else {
                opt = $('<option />', {
                    value: "N/A",
                    text: "N/A"
                });
                opt.appendTo(select_y1);
                opt.clone().appendTo(select_y2);
                select_y1.multiselect('refresh');
                try{
                    select_y2.multiselect('refresh');
                }catch (err){}

            }
        }});
}


function updateMode(y_axis, index, selectedVals) {
    var select = $("#fcst_stat_" + y_axis + "_" + index);
    select.empty();
    var selectedVariable;
        try{
            selectedVariable = $("#fcst_var_" + y_axis + "_" + index).multiselect("getChecked").val();
        }catch (err){
            selectedVariable = $("#fcst_var_" + y_axis + "_" + index+ ' option:first-child').val();
        }


    var opt,selected;
    for (var i = 0; i < listStatModelRatio.length; i++) {
        var t = listStatModelRatio[i];
        if($.isArray(selectedVals)) {
            selected = $.inArray(t, selectedVals) >= 0;
        }else{
            selected = t == selectedVals;
        }
        opt = $('<option />', {
            value: t,
            text: t,
            title: value_to_desc_map[t],
            selected: selected
        });
        opt.appendTo(select);
    }
    try {
        select.multiselect("option", "noneSelectedText", "Select ratio stat");
        select.multiselect('refresh');
    } catch (err) {
    }
    var fcst_stat_mode = $("#fcst_stat_mode_" + y_axis + "_" + index);
    var selectedModeStat="";
    var selectedModeStatCode = "";
    if( selectedVals.length > 0 && selectedVals.split("_").length == 2 ){
        selectedModeStat=selectedVals.split("_")[0];
        selectedModeStatCode = selectedVals.split("_")[1]
    }
    if(selectedModeStat == 'ACOVACOV'){
        selectedModeStat = 'ACOV';
    }
    var is_fcst_stat_mode = false;
    var is_options_empty = ( fcst_stat_mode.find('option').length == 0);
    if (!is_options_empty) {
        try{
        fcst_stat_mode.multiselect('destroy');
        } catch(err){}
    }

    for (var i = 0; i < listStatMode.length; i++) {
        var t = listStatMode[i];
        selected = (t == selectedModeStat);
        if (selected) {
            is_fcst_stat_mode = true;
        }
        if (is_options_empty) {
            opt = $('<option />', {
                value: t,
                text: t,
                title: value_to_desc_map[t],
                selected: selected
            });
            opt.appendTo(fcst_stat_mode);
        } else {
            if (selected) {
                fcst_stat_mode.val(t);
            }
        }
    }

    fcst_stat_mode.multiselect({ multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 225,
        height: 300,
        noneSelectedText: "Select attribute stat",
        allUnselected: !is_fcst_stat_mode,
        click: function (event, ui) {
            var id_array = this.id.split("_");
            //$("#fcst_stat_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("uncheckAll");
            $("#fcst_stat_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1] +" option").removeAttr("selected");
            $("#fcst_stat_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("refresh");

            var config_table = $("#fcst_stat_mode_config_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]);
            config_table.css("display", "block");
            if (ui.value == "ACOV") {
                config_table.find(".non-acov").attr("disabled", true);
            } else {
                config_table.find(".non-acov").removeAttr("disabled");
            }
            if(listStatModePair.indexOf(ui.value) > -1 ){
                config_table.find(".non-pair").attr("disabled", true);
            }else{
                config_table.find(".non-pair").removeAttr("disabled");
            }
            updateSeriesSeriesBox();
        }
    });


    if (is_fcst_stat_mode) {
        var config_table = $("#fcst_stat_mode_config_" + y_axis + "_" + index);
        config_table.css("display", "block");
        if (selectedModeStat == "ACOV") {
            config_table.find(".non-acov").attr("disabled", true);
        } else {
            config_table.find(".non-acov").removeAttr("disabled");
        }
        if (selectedModeStat == "ACOV") {
            if (selectedModeStatCode[0] == "A") {
                config_table.find('[name="mode_stat_fcst"]').prop('checked', true);
                config_table.find('[name="mode_stat_obs"]').prop('checked', true);
            } else if (selectedModeStatCode[0] == "F") {
                config_table.find('[name="mode_stat_fcst"]').prop('checked', true);
                config_table.find('[name="mode_stat_obs"]').prop('checked', false);
            } else if (selectedModeStatCode[0] == "O") {
                config_table.find('[name="mode_stat_fcst"]').prop('checked', false);
                config_table.find('[name="mode_stat_obs"]').prop('checked', true);
            }

        } else {
            if(selectedModeStatCode[0] == "D"){
                config_table.find('[name="mode_stat_diff"]').prop('checked', true);
            }else if (selectedModeStatCode[0] == "A"){
                config_table.find('[name="mode_stat_diff"]').prop('checked', false);
                config_table.find('[name="mode_stat_fcst"]').prop('checked', true);
                config_table.find('[name="mode_stat_obs"]').prop('checked', true);
            } else if (selectedModeStatCode[0] == "F") {
                config_table.find('[name="mode_stat_diff"]').prop('checked', false);
                config_table.find('[name="mode_stat_fcst"]').prop('checked', true);
                config_table.find('[name="mode_stat_obs"]').prop('checked', false);
            } else if (selectedModeStatCode[0] == "O") {
                config_table.find('[name="mode_stat_diff"]').prop('checked', false);
                config_table.find('[name="mode_stat_fcst"]').prop('checked', false);
                config_table.find('[name="mode_stat_obs"]').prop('checked', true);
            }

            if(selectedModeStatCode[1] == "A"){
                config_table.find('[name="mode_stat_simple"]').prop('checked', true);
                config_table.find('[name="mode_stat_cluster"]').prop('checked', true);
            }else if(selectedModeStatCode[1] == "S"){
                config_table.find('[name="mode_stat_simple"]').prop('checked', true);
                config_table.find('[name="mode_stat_cluster"]').prop('checked', false);

            }else if(selectedModeStatCode[1] == "C"){
                config_table.find('[name="mode_stat_simple"]').prop('checked', false);
                config_table.find('[name="mode_stat_cluster"]').prop('checked', true);
            }
            if(selectedModeStatCode[2] == "A"){
                config_table.find('[name="mode_stat_matched"]').prop('checked', true);
                config_table.find('[name="mode_stat_unmatched"]').prop('checked', true);
            }else if(selectedModeStatCode[2] == "M"){
                config_table.find('[name="mode_stat_matched"]').prop('checked', true);
                config_table.find('[name="mode_stat_unmatched"]').prop('checked', false);
            }else if(selectedModeStatCode[2] == "U"){
                config_table.find('[name="mode_stat_matched"]').prop('checked', false);
                config_table.find('[name="mode_stat_unmatched"]').prop('checked', true);
            }
        }
    }


    if (selectedVariable == "N/A") {
        try {
            select.multiselect("disable");
        } catch (err) {
        }
        try {
            fcst_stat_mode.multiselect("disable");
        } catch (err) {
        }
    } else {
        try {
            select.multiselect("enable");
        } catch (err) {
        }
        try {
            fcst_stat_mode.multiselect("enable");
        } catch (err) {
        }
    }
    selectedVals='';

    //update series_var
    select = $("#series_var_" + y_axis + "_" + index);
    select.empty();
    $.each(fix_var_value_to_title_mode_map, function (key, val) {
        select.append('<option value="' + key + '">' + val + '</option>');
    });
    try {
        select.multiselect('refresh');
    } catch (err) {

    }
}


function updateStats(y_axis, index,selectedVals) {
    var fcst_stat_select = $("#fcst_stat_" + y_axis + "_" + index);
    var fcst_stat_mode = $("#fcst_stat_mode_" + y_axis + "_" + index);
    var selected_mode = $("#plot_data").multiselect("getChecked").val();


    //get value of database


    var selectedDatabase = $("#database").multiselect("getChecked").val();
    var selectedVariable;
    try{
        selectedVariable = $("#fcst_var_" + y_axis + "_" + index).multiselect("getChecked").val();
    }catch (err){
        selectedVariable = $("#fcst_var_" + y_axis + "_" + index+ ' option:first-child').val();
    }

    if (selected_mode == "stat") {
        fcst_stat_select.empty();

        try {
            fcst_stat_mode.multiselect("destroy");
        } catch (err) {
        }
        fcst_stat_mode.css("display", "none");
        $("#fcst_stat_mode_config_" + y_axis + "_" + index).css("display", "none");
        try {
            fcst_stat_select.multiselect("option", "noneSelectedText", 'Select attribute stat');
        } catch (err) {
            fcst_stat_select.prop('disabled', 'disabled');
        }
    } else {
        try {
            fcst_stat_mode.multiselect("enable");
        } catch (err) {
            fcst_stat_mode.prop('disabled', '');
        }
        try {
            fcst_stat_select.multiselect("option", "noneSelectedText", 'Select ratio stat');
        } catch (err) {
            fcst_stat_select.prop('disabled', 'disabled');
        }
    }
    if(selectedVariable == "N/A"){
        try{
            fcst_stat_select.multiselect("disable");
        }catch (err){
            fcst_stat_select.prop('disabled', 'disabled');
        }
    } else {
        try{
            fcst_stat_select.multiselect("enable");
        }catch (err){
            fcst_stat_select.prop('disabled', '');
        }
        $.ajax({
            async: false,
            url: "servlet",
            type: "POST",
            dataType:'xml',
            data: '<request><db_con>' + selectedDatabase + '</db_con><list_stat><id>0</id><stat_fcst_var>' + selectedVariable + '</stat_fcst_var></list_stat></request>',
            error: function (jqXHR, textStatus, errorThrown) {
            },
            success: function (data) {
                var values = $(data).find("val");
                var opt, selected;
                if (values.length > 0) {
                    for (var i = 0; i < values.length; i++) {
                        var t = $(values[i]);
                        selected = $.inArray(t.text(), selectedVals) >= 0;
                        opt = $('<option />', {
                            value: t.text(),
                            text: t.text(),
                            title: value_to_desc_map[t.text()],
                            selected: selected
                        });
                        opt.appendTo(fcst_stat_select);
                    }

                } else {
                    opt = $('<option />', {
                        value: "N/A",
                        text: "N/A"
                    });
                    opt.appendTo(fcst_stat_select);

                }
                try {
                    fcst_stat_select.multiselect('refresh');
                } catch (err) {

                }
                selectedVals = [];
            }
        });
    }
    //update series_var
    var select = $("#series_var_" + y_axis + "_" + index);
    select.empty();
    $.each(series_var_value_to_title_stat_map, function (key, val) {
        select.append('<option value="' + key + '">' + val + '</option>');
    });
    try {
        select.multiselect('refresh');
    } catch (err) {

    }
}

function updateSeriesVarValEns(index, selectedVals) {
    $('#listdt').jqGrid('clearGridData');

    var select = $("#series_var_val_y1_" + index);
    select.empty();
    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();

    var selectedSeriesVariable;
    try {
        selectedSeriesVariable = $("#series_var_y1_" + index).multiselect("getChecked").val();
    } catch (err) {
        selectedSeriesVariable = $("#series_var_y1_" + index + ' option:first-child').val();
    }
    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><ensss_field>' + selectedSeriesVariable + '</ensss_field><stat><fcst_var name="TMP"></fcst_var></stat></list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {

        },
        success: function (data) {
            seriesY1VarValResponse[index] = data;
            var values = $($.parseXML(data)).find("val");
            var opt, selected;
            if (values.length > 0) {
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    selected = $.inArray(t.text(), selectedVals) >= 0;
                    opt = $('<option />', {
                        value: t.text(),
                        text: t.text(),
                        selected: selected
                    });
                    opt.appendTo(select);
                }
                try {
                    select.multiselect('refresh');
                } catch (err) {
                }
            } else {
                opt = $('<option />', {
                    value: "N/A",
                    text: "N/A"
                });
                opt.appendTo(select);
                try {
                    select.multiselect('refresh');
                } catch (err) {
                }
            }
            selectedVals = [];
        }});
}


function updateSeriesVarValSeries(y_axis, index, selectedVals) {
    var select = $("#series_var_val_" + y_axis + "_" + index);
    select.empty();
    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();
    var selected_mode = $("#plot_data").multiselect("getChecked").val();
    var selectedSeriesVariable;
    try {
        selectedSeriesVariable = $("#series_var_" + y_axis + "_" + index).multiselect("getChecked").val();
    } catch (err) {
        selectedSeriesVariable = $("#series_var_" + y_axis + "_" + index + ' option:first-child').val();
    }
    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><'+selected_mode +'_field>' + selectedSeriesVariable + '</'+selected_mode + '_field>' + convertVarsAndStatsToXmlSeries() +'</list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {

        },
        success: function (data) {
            if(y_axis == 'y1'){
                seriesY1VarValResponse[index] = data;
            }else{
                seriesY2VarValResponse[index] = data;
            }

            var values = $($.parseXML(data)).find("val");
            var opt,selected;
            if (values.length > 0) {
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    if($.type(selectedVals) == "string"){
                        selected = t.text() == selectedVals;
                    }else{
                        selected = $.inArray(t.text(), selectedVals) >= 0;
                    }

                    opt = $('<option />', {
                        value: t.text(),
                        text: t.text(),
                        selected:selected
                    });
                    opt.appendTo(select);
                }
                try {
                    select.multiselect('refresh');
                } catch (err) {
                }
            } else {
                opt = $('<option />', {
                    value: "N/A",
                    text: "N/A"
                });
                opt.appendTo(select);
                try {
                    select.multiselect('refresh');
                } catch (err) {
                }

            }
            if(selectedVals.length == 1){
                if(selectedVals[0].indexOf(",") != -1){
                    $("#group_series_var_" + y_axis + "_" + index).prop('checked', true);
                }
            }
            selectedVals=[];
        }});
}

function updateFixedVarValHist(index, selectedVals) {
    var fixed_var_val = $("#fixed_var_val_" + index);
    fixed_var_val.empty();
    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();
    var selectedFixedVariable;
    try {
        selectedFixedVariable = $("#fixed_var_" + index).multiselect("getChecked").val();
    } catch (err) {
        selectedFixedVariable = $("#fixed_var_" + index + ' option:first-child').val();
    }
    var field_type;
    if (currentTab == 'Rhist') {
        field_type = "rhist";
    } else if (currentTab == 'Phist') {
        field_type = "phist";
    } else if (currentTab == 'Roc') {
        field_type = "roc";
    } else if (currentTab == 'Rely') {
        field_type = "rely";
    } else if (currentTab == 'Ens_ss') {
        field_type = "ensss";
    }
    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        dataType: 'xml',
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><'+field_type +'_field>' + selectedFixedVariable + '</'+field_type+'_field></list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {
            console.log(errorThrown);
        },
        success: function (data) {
            fixVarValResponse[index] = data;
            var values = $(data).find("val");
            var opt, selected;
            if (values.length > 0) {
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    selected = $.inArray(t.text(), selectedVals) >= 0;
                    opt = $('<option />', {
                        value: t.text(),
                        text: t.text(),
                        selected: selected
                    });
                    opt.appendTo(fixed_var_val);
                }
                try {
                    fixed_var_val.multiselect('refresh');
                } catch (err) {
                }
            } else {
                opt = $('<option />', {
                    value: "N/A",
                    text: "N/A"
                });
                opt.appendTo(fixed_var_val);
                try {
                    fixed_var_val.multiselect('refresh');
                } catch (err) {
                }
            }
            selectedVals = [];
        }});
}

function updateFixedVarValSeries(index, selectedVals) {
    var select = $("#fixed_var_val_" +  + index);
    select.empty();
    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();
    var selectedFixedVariable;
    var selected_mode = $("#plot_data").multiselect("getChecked").val();
    try{
        selectedFixedVariable = $("#fixed_var_" + index).multiselect("getChecked").val();
    }catch (err){
        selectedFixedVariable = $("#fixed_var_" + index + ' option:first-child').val();
    }
    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        dataType:'xml',
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><'+selected_mode +'_field>' + selectedFixedVariable + '</'+selected_mode + '_field>'+ convertVarsAndStatsToXmlSeries()+ '</list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {
            select.multiselect('refresh');
        },
        success: function (data) {
            fixVarValResponse[index] = data;
            var values = $(data).find("val");

            var opt, selected;
            if (values.length > 0) {
                for (var i = 0; i < values.length; i++) {
                    var t = $(values[i]);
                    selected = $.inArray(t.text(), selectedVals) >= 0;
                    opt = $('<option />', {
                        value: t.text(),
                        text: t.text(),
                        selected: selected
                    });
                    opt.appendTo(select);
                }
                try {
                    select.multiselect('refresh');
                } catch (err) {
                }
            } else {
                opt = $('<option />', {
                    value: "N/A",
                    text: "N/A"
                });
                opt.appendTo(select);
                try {
                    select.multiselect('refresh');
                } catch (err) {
                }
            }
            selectedVals=[];
        }
    });
}
function populateIndyVarVal(selectedVals) {
    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();
    var selectedFixedVariable;
    var selected_mode = $("#plot_data").multiselect("getChecked").val();
    try {
        selectedFixedVariable = $("#indy_var").multiselect("getChecked").val();
    } catch (err) {
        selectedFixedVariable = $("#indy_var" + ' option:first-child').val();
    }
    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        dataType: 'xml',
        data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><' + selected_mode + '_field>' + selectedFixedVariable + '</' + selected_mode + '_field>' + convertVarsAndStatsToXmlSeries() + '</list_val></request>',
        error: function (jqXHR, textStatus, errorThrown) {

        },
        success: function (data) {
            var values = $(data).find("val");
            if (data != previousIndVarValResponse) {
                previousIndVarValResponse = data;
                var select = $("#indy_var_val");

                var indy_var_val = $('[name="multiselect_indy_var_val"]');
                if (indy_var_val && indy_var_val.length > 0) {
                    indy_var_vals_to_attr = {};
                    for (var i = 0; i < indy_var_val.length; i++) {
                        var jqObject = $(indy_var_val[i]);
                        var obj = {};
                        var id = jqObject.attr("id");
                        obj.label = $('#' + id + '_label').val();
                        obj.plot_val = $('#' + id + '_plot_val').val();
                        indy_var_vals_to_attr[indy_var_val[i].value] = obj;
                    }
                }
                $("#indy_var_val").multiselect("option", "indy_var_vals_to_attr", indy_var_vals_to_attr);
                select.empty();
                var opt, selected;
                if (values.length > 0) {
                    for (var i = 0; i < values.length; i++) {
                        var t = $(values[i]);
                        selected = $.inArray(t.text(), selectedVals) >= 0;
                        opt = $('<option />', {
                            value: t.text(),
                            text: t.text(),
                            selected: selected,
                            class: "indy-var-option"
                        });
                        opt.appendTo(select);
                    }
                    try {
                        select.multiselect('refresh');
                        if ( !(indy_var_val && indy_var_val.length > 0) ) {
                            $("#indy_var_val").multiselect("option", "indy_var_vals_to_attr", null);
                        }
                    } catch (err) {
                    }
                } else {
                    opt = $('<option />', {
                        value: "N/A",
                        text: "N/A"
                    });
                    opt.appendTo(select);
                    try {
                        select.multiselect('refresh');
                    } catch (err) {
                    }
                }
            }
        }});
}
function convertVarsAndStatsToXmlSeries(){
    var result = "<stat>";
    var fcst_var;
    for(var i=0; i< fcst_var_y1_indexes.length; i++){
        fcst_var = convertVarAndStatsForAxisToXml("y1", fcst_var_y1_indexes[i]);
        result = result + fcst_var;
    }
    for(var i=0; i< fcst_var_y2_indexes.length; i++){
            fcst_var = convertVarAndStatsForAxisToXml("y2", fcst_var_y2_indexes[i]);
            result = result + fcst_var;
        }
    return  result + "</stat>";
}

function convertVarAndStatsForAxisToXml(y_axis, index) {
    var selectedVariable;
    try {
        selectedVariable = $("#fcst_var_" + y_axis + "_" + index).multiselect("getChecked").val();
    } catch (err) {
        selectedVariable = $("#fcst_var_" + y_axis + "_" + index + ' option:first-child').val();
    }
    var fcst_var = '<fcst_var name="' + selectedVariable + '" >';
    var fcst_stats = $("#fcst_stat_" + y_axis + "_" + index).val();
    if (fcst_stats) {
        for (var j = 0; j < fcst_stats.length; j++) {
            fcst_var = fcst_var + '<val>' + fcst_stats[j] + '</val>';
        }
    }
    fcst_var = fcst_var + '</fcst_var>';
    return fcst_var;
}

function createStatNameForModeArrt(stat_name, y_axis, fcst_var_index) {
    var fcst_stat_mode_config = $("#fcst_stat_mode_config_" + y_axis + "_" + fcst_var_index);
    //this is mode attribute stat - add code
    var boolDiff = fcst_stat_mode_config.find('[name="mode_stat_diff"]').is(':checked');
    var boolFcst = fcst_stat_mode_config.find('[name="mode_stat_fcst"]').is(':checked');
    var boolObs = fcst_stat_mode_config.find('[name="mode_stat_obs"]').is(':checked');
    var strCode = "_";
    if (listStatModeSingle.indexOf(stat_name) > -1) {
        if (boolDiff) {
            strCode += "D";
        } else if (boolFcst && boolObs) {
            strCode += "A";
        } else if (boolFcst) {
            strCode += "F";
        } else if (boolObs) {
            strCode += "O";
        } else {
            strCode += "A";
        }
    }
    //  if the stat is ACOV, return the code
    if (stat_name.startsWith("ACOV")) {
        strCode = stat_name + strCode + "SA";
    }
    //  determine the second letter of the code [A|S|C]
    var boolSimp = fcst_stat_mode_config.find('[name="mode_stat_simple"]').is(':checked');
    var boolClus = fcst_stat_mode_config.find('[name="mode_stat_cluster"]').is(':checked');
    if (boolSimp && boolClus) {
        strCode += "A";
    } else if (boolSimp) {
        strCode += "S";
    } else if (boolClus) {
        strCode += "C";
    } else {
        strCode += "A";
    }
    //  determine the third letter of the code [A|M|U]
    var boolMat = fcst_stat_mode_config.find('[name="mode_stat_matched"]').is(':checked');
    var boolUnm = fcst_stat_mode_config.find('[name="mode_stat_unmatched"]').is(':checked');
    if (boolMat && boolUnm) {
        strCode += "A";
    } else if (boolMat) {
        strCode += "M";
    } else if (boolUnm) {
        strCode += "U";
    } else {
        strCode += "A";
    }
    return (stat_name + strCode);

}

function createMapForForecastVar(y_axis, fcst_var_indexes, selected_mode) {
    var forecast_var_to_stat_map = {};
    var stat;
    for (var i = 0; i < fcst_var_indexes.length; i++) {
        var fcst_var = $("#fcst_var_y" + y_axis + "_" + fcst_var_indexes[i]).val();
        if (!forecast_var_to_stat_map.hasOwnProperty(fcst_var)) {
            forecast_var_to_stat_map[fcst_var] = [];
        }
        var statsArr = $("#fcst_stat_y" + y_axis + "_" + fcst_var_indexes[i]).val();
        if (statsArr != null) {
            for (var j = 0; j < statsArr.length; j++) {
                if (selected_mode == "mode" && listStatModelRatio.indexOf(statsArr[j]) == -1) {
                    stat = createStatNameForModeArrt(statsArr[j], "y" + y_axis, fcst_var_indexes[i]);
                } else {
                    stat = statsArr[j];
                }
                forecast_var_to_stat_map[fcst_var].push(stat);
            }
        } else if (selected_mode == "mode") {
            statsArr = $("#fcst_stat_mode_y" + y_axis + "_" + fcst_var_indexes[i]).val();
            if (statsArr != null) {
                stat = createStatNameForModeArrt(statsArr, "y" + y_axis, fcst_var_indexes[i]);
                forecast_var_to_stat_map[fcst_var].push(stat);
            }
        }
    }
    return forecast_var_to_stat_map;
}

function createSeriesMapForPermutation(fcst_var_indexes, y_axis) {
    var series_var_to_values_map = {};
    var selected_series_val, selected_series, isGroup, seriesName;

    for (var i = fcst_var_indexes.length-1; i >= 0; i--) {
        var listVal = [];
        selected_series = $("#series_var_" + y_axis + "_" + fcst_var_indexes[i]).multiselect("getChecked")[0].value;
        isGroup = $("#group_series_var_" + y_axis + "_" + fcst_var_indexes[i]).is(':checked');
        try {
            selected_series_val = $("#series_var_val_" + y_axis + "_" + fcst_var_indexes[i]).multiselect("getChecked");
        } catch (err) {
            selected_series_val = [];
        }
        for (var j = 0; j < selected_series_val.length; j++) {
            listVal.push(selected_series_val[j].value);
        }
        if (isGroup) {
            seriesName = "Group_" + y_axis + "_" + fcst_var_indexes[i];
            group_name_to_value_map[seriesName] = listVal;
            listVal = [seriesName];
        }
        var strValCur = series_var_to_values_map[selected_series];
        if (undefined != strValCur) {
            //listVal = strValCur.concat(listVal);
            listVal = listVal.concat(strValCur);
        }
        series_var_to_values_map[selected_series] = listVal;
    }
    return series_var_to_values_map;
}
function createSeriesMapForPermutationEns(fcst_var_indexes, y_axis) {
    var series_var_to_values_map = {};
    var selected_series_val, selected_series, seriesName;

    for (var i = 0; i < fcst_var_indexes.length; i++) {
        var listVal = [];
        try {
            selected_series = $("#series_var_" + y_axis + "_" + fcst_var_indexes[i]).multiselect("getChecked")[0].value;
        } catch (err) {
            selected_series = $("#series_var_" + y_axis + "_" + fcst_var_indexes[i]).val();
        }
        try {
            selected_series_val = $("#series_var_val_" + y_axis + "_" + fcst_var_indexes[i]).multiselect("getChecked");
        } catch (err) {
            selected_series_val = [];
        }
        for (var j = 0; j < selected_series_val.length; j++) {
            listVal.push(selected_series_val[j].value);
        }

        var strValCur = series_var_to_values_map[selected_series];
        if (undefined != strValCur) {
            listVal = strValCur.concat(listVal);
        }
        series_var_to_values_map[selected_series] = listVal;
    }
    return series_var_to_values_map;
}
function updateSeriesRhist() {
    var table = $("#listdt");
    table.saveCell(lastSelRow, lastSelCol);
    var oldSeriesData = table.jqGrid('getRowData');
    table.jqGrid('clearGridData');

    var series_perm;
    var number_series = 0;
    var series_formatting = {};
    var isFixedFormatting = $("#seriesLock").is(':checked');
    var newSeriesData = [];
    var y_axis="Y1";
    var plot_ci = [];
    var plot_disp = [];
    var show_signif = [];
    var colors = [];
    var pch = [];
    var lty = [];
    var lwd = [];
    var con_series = [];
    var order_series = [];
    var legend = [];
    var type = [];
    var stat_name ;
    if (currentTab == 'Rhist' || currentTab == 'Phist'){
        stat_name = "Rank Histogram";
    }else if(currentTab == 'Roc'){
        stat_name = "ROC Curve";
    }else if(currentTab == 'Rely'){
        stat_name = "Reliability Curve";
    }

    if (initXML != null) {
        plot_ci = initXML.find("plot").find("plot_ci").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        plot_disp = initXML.find("plot").find("plot_disp").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        show_signif = initXML.find("plot").find("show_signif").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        for (var i = 0; i < plot_disp.length; i++) {
            if (plot_disp[i].trim() == "TRUE") {
                plot_disp[i] = "No";
            } else {
                plot_disp[i] = "Yes";
            }
            if (show_signif[i].trim() == "TRUE") {
                show_signif[i] = "No";
            } else {
                show_signif[i] = "Yes";
            }
        }

        colors = initXML.find("plot").find("colors").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        for (var i = 0; i < colors.length; i++) {
            colors[i] = colors[i].replace(/FF$/, "");
        }
        pch = initXML.find("plot").find("pch").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        lty = initXML.find("plot").find("lty").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        lwd = initXML.find("plot").find("lwd").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        con_series = initXML.find("plot").find("con_series").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        order_series = initXML.find("plot").find("order_series").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        type = initXML.find("plot").find("type").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        legend = initXML.find("plot").find("legend").text().replace("c(", "").replace(/\)$/, "").replace(/"/g, "").split(",");
        if (legend.length != order_series.length) {
            for (var k = 0; k < order_series.length; k++) {
                legend[k] = "";
            }
        }
    }


    series_perm = permuteSeriesNew(createSeriesMapForPermutationEns(series_var_y1_indexes, "y1"), 0);
    var seriesName;
    if (series_perm.length == 0) {
        series_perm[0] = "";
    }
    for (var series_perm_index = 0; series_perm_index < series_perm.length; series_perm_index++) {
        seriesName = series_perm[series_perm_index] + " " + stat_name;
        //check if this series was their before
        var isSeriesOld = false;
        for (var i = 0; i < oldSeriesData.length; i++) {
            if (oldSeriesData[i]['title'] == seriesName) {
                series_formatting = {};
                series_formatting.title = seriesName;
                series_formatting.y_axis = y_axis;
                series_formatting.order = number_series + 1;
                series_formatting.hide = oldSeriesData[i]['hide'];
                series_formatting.plot_ci = oldSeriesData[i]['plot_ci'];
                series_formatting.show_signif = oldSeriesData[i]['show_signif'];
                series_formatting.color = oldSeriesData[i]['color'];
                series_formatting.pch = oldSeriesData[i]['pch'];
                series_formatting.type = oldSeriesData[i]['type'];
                series_formatting.lty = oldSeriesData[i]['lty'];
                series_formatting.lwd = oldSeriesData[i]['lwd'];
                series_formatting.con_series = oldSeriesData[i]['con_series'];
                series_formatting.legend = oldSeriesData[i]['legend'];
                series_formatting.id = number_series + 1;
                isSeriesOld = true;
                break;
            }
        }
        //if it is a new series
        if (!isSeriesOld) {
            //check if it is the first
            if (isFixedFormatting && number_series > 0 && initXML == null) {
                series_formatting = jQuery.extend(true, {}, newSeriesData[newSeriesData.length - 1]);
                series_formatting.title = seriesName;
                series_formatting.y_axis = y_axis;
                series_formatting.id = number_series + 1;
                series_formatting.order = number_series + 1;
            } else {
                if (initXML != null) {
                    series_formatting = {};
                    series_formatting.title = seriesName;
                    series_formatting.y_axis = y_axis;
                    if(!order_series[number_series] || order_series[number_series] == ''){
                        series_formatting.order = parseInt( i+1 );
                    }else{
                        series_formatting.order = parseInt(order_series[number_series]);
                    }
                    series_formatting.hide = plot_disp[number_series];
                    series_formatting.plot_ci = plot_ci[number_series].trim();
                    series_formatting.show_signif = show_signif[number_series];
                    series_formatting.pch = pch[number_series].trim();
                    series_formatting.type = type[number_series].trim();
                    series_formatting.lty = lty[number_series].trim();
                    series_formatting.lwd = lwd[number_series].trim();
                    series_formatting.con_series = con_series[number_series].trim();
                    series_formatting.legend = legend[number_series].trim();
                    series_formatting.color = colors[number_series].trim();
                    series_formatting.id = number_series + 1;

                } else {
                    series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
                    series_formatting.title = seriesName;
                    series_formatting.y_axis = y_axis;
                    series_formatting.id = number_series + 1;
                    series_formatting.order = number_series + 1;
                }
            }
        }
        number_series++;
        newSeriesData.push(series_formatting);
    }


    //set default color for each series if it is not a upload
    if (initXML == null) {
        colors = rainbow(newSeriesData.length);
        for (var i = 0; i < newSeriesData.length; i++) {
            newSeriesData[i].color = colors[i];
        }
    }

    newSeriesData.sort(SortByOrder);
    for (var i = 0; i < newSeriesData.length; i++) {
        table.jqGrid('addRowData', i + 1, newSeriesData[i]);
    }
    outerLayout.sizePane("south", $('#gbox_listdt').height())
}


function updateSeriesVarValRhist(index, selectedVals) {
    $('#listdt').jqGrid('clearGridData');
    var select = $("#series_var_val_y1_" + index);
    select.empty();
    //get value of database
    var selectedDatabase = $("#database").multiselect("getChecked").val();

    var selectedSeriesVariable;
    try {
        selectedSeriesVariable = $("#series_var_y1_" + index).multiselect("getChecked").val();
    } catch (err) {
        selectedSeriesVariable = $("#series_var_y1_" + index + ' option:first-child').val();
    }
    var fcst_var = "TMP";
    if (fcst_vars.length > 0) {
        fcst_var = fcst_vars[0];
    }
    var fcst_vars_stat = "BASER";
    if(fcst_vars_stats.length > 0){
        fcst_vars_stat = fcst_vars_stats[0];
    }
    $.ajax({
            async: false,
            url: "servlet",
            type: "POST",
        //<request><db_con>mv_dart_gsi</db_con><list_val><id>0</id><stat_field>fcst_valid_beg</stat_field><stat><fcst_var name="TMP" ><val>BCRMSE</val></fcst_var></stat></list_val></request>
            data: '<request><db_con>' + selectedDatabase + '</db_con><list_val><id>0</id><stat_field>' + selectedSeriesVariable + '</stat_field><stat><fcst_var name="'+ fcst_var+ '"><val>'+fcst_vars_stat +'</val></fcst_var></stat></list_val></request>',
            error: function (jqXHR, textStatus, errorThrown) {

            },
            success: function (data) {
                seriesY1VarValResponse[index] = data;
                var values = $($.parseXML(data)).find("val");
                var opt, selected;
                if (values.length > 0) {
                    for (var i = 0; i < values.length; i++) {
                        var t = $(values[i]);
                        selected = $.inArray(t.text(), selectedVals) >= 0;
                        opt = $('<option />', {
                            value: t.text(),
                            text: t.text(),
                            selected: selected
                        });
                        opt.appendTo(select);
                    }
                    try {
                        select.multiselect('refresh');
                    } catch (err) {
                    }
                } else {
                    opt = $('<option />', {
                        value: "N/A",
                        text: "N/A"
                    });
                    opt.appendTo(select);
                    try {
                        select.multiselect('refresh');
                    } catch (err) {
                    }
                }
                selectedVals = [];
            }});
}
function updateSeriesRoc() {
    $('#listdt').jqGrid('clearGridData');
    var series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
    series_formatting.title = 'ROC Curve';
    series_formatting.y_axis = "Y1";
    series_formatting.order = 1;
    series_formatting.color = '#AAAAAA';
    series_formatting.id = 1;
    $("#listdt").jqGrid('addRowData', 1, series_formatting);
}

function updateSeriesRely() {
    var rely_event_hist = $("input:radio[name='rely_event_hist']:checked").val();
    var table = $("#listdt");
    table.jqGrid('clearGridData');
    var series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
    series_formatting.title = 'Reliability Curve';
    series_formatting.y_axis = "Y1";
    series_formatting.order = 1;
    series_formatting.color = '#333333';
    series_formatting.id = 1;
    table.jqGrid('addRowData', 1, series_formatting);
    if (rely_event_hist == "true") {
        series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
        series_formatting.title = 'Event Histogram';
        series_formatting.y_axis = "Y1";
        series_formatting.order = 2;
        series_formatting.color = '#AAAAAA';
        series_formatting.id = 2;
        series_formatting.type = "h";
        series_formatting.lwd = 50;
        table.jqGrid('addRowData', 2, series_formatting);
    }
}


function updateSeriesEns() {
    var table = $("#listdt");
    table.saveCell(lastSelRow, lastSelCol);
    var oldSeriesData = table.jqGrid('getRowData');
    table.jqGrid('clearGridData');

    var series_perm;
    var number_series = 0;
    var series_formatting = {};
    var isFixedFormatting = $("#seriesLock").is(':checked');
    var newSeriesData = [];
    var y_axis;
    var plot_ci = [];
    var plot_disp = [];
    var show_signif = [];
    var colors = [];
    var pch = [];
    var lty = [];
    var lwd = [];
    var con_series = [];
    var order_series = [];
    var legend = [];
    var type = [];

    if (initXML != null) {
        plot_ci = initXML.find("plot").find("plot_ci").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        plot_disp = initXML.find("plot").find("plot_disp").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        show_signif = initXML.find("plot").find("show_signif").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        for (var i = 0; i < plot_disp.length; i++) {
            if (plot_disp[i].trim() == "TRUE") {
                plot_disp[i] = "No";
            } else {
                plot_disp[i] = "Yes";
            }
            if (show_signif[i].trim() == "TRUE") {
                show_signif[i] = "No";
            } else {
                show_signif[i] = "Yes";
            }
        }

        colors = initXML.find("plot").find("colors").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        for (var i = 0; i < colors.length; i++) {
            colors[i] = colors[i].replace(/FF$/, "");
        }
        pch = initXML.find("plot").find("pch").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        lty = initXML.find("plot").find("lty").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        lwd = initXML.find("plot").find("lwd").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        con_series = initXML.find("plot").find("con_series").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        order_series = initXML.find("plot").find("order_series").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        type = initXML.find("plot").find("type").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        legend = initXML.find("plot").find("legend").text().replace("c(", "").replace(/\)$/, "").replace(/"/g, "").split(",");
        if (legend.length != order_series.length) {
            for (var k = 0; k < order_series.length; k++) {
                legend[k] = "";
            }
        }
    }


    series_perm = permuteSeriesNew(createSeriesMapForPermutationEns(series_var_y1_indexes, "y1"), 0);

    var ensss_pts_disp = $("input:radio[name='ensss_pts_disp']:checked").val();
    for (var index = 0; index < 2; index++) {
        if (index == 0 || (index == 1 && ensss_pts_disp == "true")) {
            var stat_name;
            if (index == 0) {
                stat_name = "MSE";
                y_axis = "Y1";
            } else {

                stat_name = "#PTS";
                y_axis = "Y2";
            }

            for (var series_perm_index = 0; series_perm_index < series_perm.length; series_perm_index++) {
                var seriesName = series_perm[series_perm_index] + " " + stat_name;
                //check if this series was their before
                var isSeriesOld = false;
                for (var i = 0; i < oldSeriesData.length; i++) {
                    if (oldSeriesData[i]['title'] == seriesName) {
                        series_formatting = {};
                        series_formatting.title = seriesName;
                        series_formatting.y_axis = y_axis;
                        series_formatting.order = number_series + 1;
                        series_formatting.hide = oldSeriesData[i]['hide'];
                        series_formatting.plot_ci = oldSeriesData[i]['plot_ci'];
                        series_formatting.show_signif = oldSeriesData[i]['show_signif'];
                        series_formatting.color = oldSeriesData[i]['color'];
                        series_formatting.pch = oldSeriesData[i]['pch'];
                        series_formatting.type = oldSeriesData[i]['type'];
                        series_formatting.lty = oldSeriesData[i]['lty'];
                        series_formatting.lwd = oldSeriesData[i]['lwd'];
                        series_formatting.con_series = oldSeriesData[i]['con_series'];
                        series_formatting.legend = oldSeriesData[i]['legend'];
                        series_formatting.id = number_series + 1;
                        isSeriesOld = true;
                        break;
                    }
                }
                //if it is a new series
                if (!isSeriesOld) {
                    //check if it is the first
                    if (isFixedFormatting && number_series > 0 && initXML == null) {
                        series_formatting = jQuery.extend(true, {}, newSeriesData[newSeriesData.length - 1]);
                        series_formatting.title = seriesName;
                        series_formatting.y_axis = y_axis;
                        series_formatting.id = number_series + 1;
                        series_formatting.order = number_series + 1;
                    } else {
                        if (initXML != null) {
                            series_formatting = {};
                            series_formatting.title = seriesName;
                            series_formatting.y_axis = y_axis;
                            if (!order_series[number_series] || order_series[number_series] == '') {
                                series_formatting.order = parseInt(i + 1);
                            } else {
                                series_formatting.order = parseInt(order_series[number_series]);
                            }
                            series_formatting.hide = plot_disp[number_series];
                            series_formatting.plot_ci = plot_ci[number_series].trim();
                            series_formatting.show_signif = show_signif[number_series];
                            series_formatting.pch = pch[number_series].trim();
                            series_formatting.type = type[number_series].trim();
                            series_formatting.lty = lty[number_series].trim();
                            series_formatting.lwd = lwd[number_series].trim();
                            series_formatting.con_series = con_series[number_series].trim();
                            series_formatting.legend = legend[number_series].trim();
                            series_formatting.color = colors[number_series].trim();
                            series_formatting.id = number_series + 1;

                        } else {
                            series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
                            series_formatting.title = seriesName;
                            series_formatting.y_axis = y_axis;
                            series_formatting.id = number_series + 1;
                            series_formatting.order = number_series + 1;
                        }
                    }
                }
                number_series++;
                newSeriesData.push(series_formatting);
            }
        }
    }

    //set default color for each series if it is not a upload
    if (initXML == null) {
        colors = rainbow(newSeriesData.length);
        for (var i = 0; i < newSeriesData.length; i++) {
            newSeriesData[i].color = colors[i];
        }
    }

    newSeriesData.sort(SortByOrder);
    for (var i = 0; i < newSeriesData.length; i++) {
        table.jqGrid('addRowData', i + 1, newSeriesData[i]);
    }
    outerLayout.sizePane("south", $('#gbox_listdt').height());
}


function updateSeriesSeriesBox(isCheckAll) {
    var table = $("#listdt");
    table.saveCell(lastSelRow, lastSelCol);
    var oldSeriesData = table.jqGrid('getRowData');
    table.jqGrid('clearGridData');
    group_name_to_value_map = {};

    var series_perm;
    var number_series = 0;
    var series_formatting = {};
    var isFixedFormatting = $("#seriesLock").is(':checked');
    var newSeriesData = [];
    var selected_mode = $("#plot_data").multiselect("getChecked").val();
    var y_axis;
    var fcst_var_indexes;
    var plot_ci = [];
    var plot_disp = [];
    var show_signif = [];
    var colors = [];
    var pch = [];
    var lty = [];
    var lwd = [];
    var con_series = [];
    var order_series = [];
    var legend = [];
    var type = [];

    if (initXML != null) {
        plot_ci = initXML.find("plot").find("plot_ci").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        plot_disp = initXML.find("plot").find("plot_disp").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        show_signif = initXML.find("plot").find("show_signif").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        for (var i = 0; i < plot_disp.length; i++) {
            if (plot_disp[i].trim() == "TRUE") {
                plot_disp[i] = "No";
            } else {
                plot_disp[i] = "Yes";
            }
            if (show_signif[i].trim() == "TRUE") {
                show_signif[i] = "Yes";
            } else {
                show_signif[i] = "No";
            }
        }

        colors = initXML.find("plot").find("colors").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        for (var i = 0; i < colors.length; i++) {
            colors[i] = colors[i].replace(/FF$/, "");
        }
        pch = initXML.find("plot").find("pch").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        lty = initXML.find("plot").find("lty").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        lwd = initXML.find("plot").find("lwd").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        con_series = initXML.find("plot").find("con_series").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        var order_series_el=initXML.find("plot").find("order_series").text();
        if(order_series_el && order_series_el.length >0){
            order_series = initXML.find("plot").find("order_series").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        }else{
            order_series = [];
            for(var i=0; i< colors.length; i++){
                order_series.push(i+1);
            }
        }


        type = initXML.find("plot").find("type").text().replace("c(", "").replace(")", "").replace(/"/g, "").split(",");
        legend = initXML.find("plot").find("legend").text().replace("c(", "").replace(/\)$/, "").replace(/"/g, "").split(",");
        if (legend.length != order_series.length) {
            for (var k = 0; k < order_series.length; k++) {
                legend[k] = "";
            }
        }
    }
    for (var axis_index = 1; axis_index <= 2; axis_index++) {
        y_axis = "y" + axis_index;
        if (axis_index == 1) {
            fcst_var_indexes = fcst_var_y1_indexes;
            series_perm = permuteSeriesNew(createSeriesMapForPermutation(series_var_y1_indexes, y_axis), 0);
        } else {
            fcst_var_indexes = fcst_var_y2_indexes;
            series_perm = permuteSeriesNew(createSeriesMapForPermutation(series_var_y2_indexes, y_axis), 0);

        }
        for (var fcst_var_index = 0; fcst_var_index < fcst_var_indexes.length; fcst_var_index++) {
            var fcst_var = $("#fcst_var_" + y_axis + "_" + fcst_var_indexes[fcst_var_index]).val();
            var list_stats = [];
            var listSelectedBoxes;
            listSelectedBoxes = $("#fcst_stat_" + y_axis + "_" + fcst_var_indexes[fcst_var_index]).multiselect("getChecked");
            if (listSelectedBoxes.length == 0 && selected_mode == "mode") {
                listSelectedBoxes = $("#fcst_stat_mode_" + y_axis + "_" + fcst_var_indexes[fcst_var_index]).multiselect("getChecked");
            }
            for (var i = 0; i < listSelectedBoxes.length; i++) {
                list_stats.push(listSelectedBoxes[i].value);
            }
            for (var list_stats_index = 0; list_stats_index < list_stats.length; list_stats_index++) {

                for (var series_perm_index = 0; series_perm_index < series_perm.length; series_perm_index++) {
                    var stat_name = list_stats[list_stats_index];
                    if (selected_mode == "mode" && listStatModelRatio.indexOf(stat_name) == -1) {
                        stat_name = createStatNameForModeArrt(stat_name, y_axis, fcst_var_indexes[fcst_var_index])
                    }
                    var seriesName = series_perm[series_perm_index] + " " + fcst_var + " " + stat_name;


                    //check if this series was their before
                    var isSeriesOld = false;
                    for (var i = 0; i < oldSeriesData.length; i++) {
                        if (oldSeriesData[i]['title'] == seriesName && oldSeriesData[i]['y_axis'].match(axis_index + "$") == axis_index) {
                            series_formatting = {};
                            series_formatting.title = seriesName;
                            series_formatting.y_axis = "Y" + axis_index;
                            series_formatting.order = number_series + 1;
                            series_formatting.hide = oldSeriesData[i]['hide'];
                            series_formatting.plot_ci = oldSeriesData[i]['plot_ci'];
                            series_formatting.show_signif = oldSeriesData[i]['show_signif'];
                            series_formatting.color = oldSeriesData[i]['color'];
                            series_formatting.pch = oldSeriesData[i]['pch'];
                            series_formatting.type = oldSeriesData[i]['type'];
                            series_formatting.lty = oldSeriesData[i]['lty'];
                            series_formatting.lwd = oldSeriesData[i]['lwd'];
                            series_formatting.con_series = oldSeriesData[i]['con_series'];
                            series_formatting.legend = oldSeriesData[i]['legend'];
                            series_formatting.id = number_series + 1;
                            isSeriesOld = true;
                            break;
                        }
                    }
                    //if it is a new series
                    if (!isSeriesOld) {
                        //check if it is the first
                        if (isFixedFormatting && number_series > 0 && initXML == null) {
                            series_formatting = jQuery.extend(true, {}, newSeriesData[newSeriesData.length - 1]);
                            series_formatting.title = seriesName;
                            series_formatting.y_axis = "Y" + axis_index;
                            series_formatting.id = number_series + 1;
                            series_formatting.order = number_series + 1;
                        } else {
                            if (initXML != null) {
                                series_formatting = {};
                                series_formatting.title = seriesName;
                                series_formatting.y_axis = "Y" + axis_index;
                                series_formatting.order = parseInt(order_series[number_series]);
                                series_formatting.hide = plot_disp[number_series];
                                series_formatting.plot_ci = plot_ci[number_series].trim();
                                series_formatting.show_signif = show_signif[number_series];
                                series_formatting.pch = pch[number_series].trim();
                                series_formatting.type = type[number_series].trim();
                                series_formatting.lty = lty[number_series].trim();
                                series_formatting.lwd = lwd[number_series].trim();
                                series_formatting.con_series = con_series[number_series].trim();
                                series_formatting.legend = legend[number_series].trim();
                                series_formatting.color = colors[number_series].trim();
                                series_formatting.id = number_series + 1;

                            } else {
                                series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
                                series_formatting.title = seriesName;
                                series_formatting.y_axis = "Y" + axis_index;
                                series_formatting.id = number_series + 1;
                                series_formatting.order = number_series + 1;
                            }
                        }
                    }
                    number_series++;
                    newSeriesData.push(series_formatting);
                }
            }
        }
    }

    var seriesDiff = [];
    var diffSeries1, diffSeries2,isDiffSeries1,isDiffSeries2,strSeriesName;
    for (var i = 0; i < seriesDiffY1.length; i++) {
        diffSeries1 = seriesDiffY1[i].split(",")[0];
        diffSeries2 = seriesDiffY1[i].split(",")[1];
        isDiffSeries1 = false;
        isDiffSeries2 = false;
        for (var j = 0; j < newSeriesData.length; j++) {
            if (diffSeries1 == newSeriesData[j].title) {
                isDiffSeries1 = true;
            }
            if (diffSeries2 == newSeriesData[j].title) {
                isDiffSeries2 = true;
            }
        }
        if (isDiffSeries1 && isDiffSeries2) {
            seriesDiff.push(seriesDiffY1[i]);
            strSeriesName = 'DIFF ("' + diffSeries1 + '"-"' + diffSeries2 + '")';
            if (isFixedFormatting && initXML == null) {
                series_formatting = jQuery.extend(true, {}, newSeriesData[newSeriesData.length - 1]);
                series_formatting.title = strSeriesName;
                series_formatting.y_axis = "Y1";
                series_formatting.id = number_series + 1;
                series_formatting.order = number_series + 1;
            } else {
                if (initXML != null) {
                    series_formatting = {};
                    series_formatting.title = strSeriesName;
                    series_formatting.y_axis = "Y1";
                    series_formatting.order = parseInt(order_series[number_series]);
                    series_formatting.hide = plot_disp[number_series];
                    series_formatting.plot_ci = plot_ci[number_series].trim();
                    series_formatting.show_signif = show_signif[number_series].trim();
                    series_formatting.pch = pch[number_series].trim();
                    series_formatting.type = type[number_series].trim();
                    series_formatting.lty = lty[number_series].trim();
                    series_formatting.lwd = lwd[number_series].trim();
                    series_formatting.con_series = con_series[number_series].trim();
                    series_formatting.legend = legend[number_series].trim();
                    series_formatting.color = colors[number_series].trim();
                    series_formatting.id = number_series + 1;

                } else {
                    series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
                    series_formatting.title = strSeriesName;
                    series_formatting.y_axis = "Y1";
                    series_formatting.id = number_series + 1;
                    series_formatting.order = number_series + 1;
                }
            }
            number_series++;
            newSeriesData.push(series_formatting);
        }
    }
    seriesDiffY1 = seriesDiff;
    seriesDiff = [];
    for (var i = 0; i < seriesDiffY2.length; i++) {
        diffSeries1 = seriesDiffY2[i].split(",")[0];
        diffSeries2 = seriesDiffY2[i].split(",")[1];
        isDiffSeries1 = false;
        isDiffSeries2 = false;
        for (var j = 0; j < newSeriesData.length; j++) {
            if (diffSeries1 == newSeriesData[j].title) {
                isDiffSeries1 = true;
            }
            if (diffSeries2 == newSeriesData[j].title) {
                isDiffSeries2 = true;
            }
        }
        if (isDiffSeries1 && isDiffSeries2) {
            seriesDiff.push(seriesDiffY2[i]);
            strSeriesName = 'DIFF ("' + diffSeries1 + '"-"' + diffSeries2 + '")';

            if (isFixedFormatting && initXML == null) {
                series_formatting = jQuery.extend(true, {}, newSeriesData[newSeriesData.length - 1]);
                series_formatting.title = strSeriesName;
                series_formatting.y_axis = "Y2";
                series_formatting.id = number_series + 1;
                series_formatting.order = number_series + 1;
            } else {
                if (initXML != null) {
                    series_formatting = {};
                    series_formatting.title = strSeriesName;
                    series_formatting.y_axis = "Y2";
                    series_formatting.order = parseInt(order_series[number_series]);
                    series_formatting.hide = plot_disp[number_series];
                    series_formatting.plot_ci = plot_ci[number_series].trim();
                    series_formatting.show_signif = show_signif[number_series];
                    series_formatting.pch = pch[number_series].trim();
                    series_formatting.type = type[number_series].trim();
                    series_formatting.lty = lty[number_series].trim();
                    series_formatting.lwd = lwd[number_series].trim();
                    series_formatting.con_series = con_series[number_series].trim();
                    series_formatting.legend = legend[number_series].trim();
                    series_formatting.color = colors[number_series].trim();
                    series_formatting.id = number_series + 1;

                } else {
                    series_formatting = jQuery.extend(true, {}, firstSeriesFormatting);
                    series_formatting.title = strSeriesName;
                    series_formatting.y_axis = "Y2";
                    series_formatting.id = number_series + 1;
                    series_formatting.order = number_series + 1;
                }
            }
            number_series++;
            newSeriesData.push(series_formatting);
        }
    }
    seriesDiffY2 = seriesDiff;


    //set default color for each series if it is not a upload
    if (initXML == null) {
        colors = rainbow(newSeriesData.length);
        var colors_new = [];
        if (isCheckAll) {
            colors_new = colors;
        } else {
            for (var i = 0; i < colors.length; i++) {
                for (var j = 0; j < newSeriesData.length; j++) {
                    if (newSeriesData[j].color && newSeriesData[j].color.length > 0
                            && newSeriesData[j].color.substr(newSeriesData[j].color.length - 6).toUpperCase() === colors[i].substr(colors[i].length - 6).toUpperCase()
                            && i != j) {
                        colors_new[j] = colors[i];
                        colors_new[i] = colors[j];
                    }
                }
            }
            for (var i = 0; i < colors.length; i++) {
                if (!colors_new[i]) {
                    colors_new[i] = colors[i];
                }
            }
        }
        for (var i = 0; i < newSeriesData.length; i++) {
            if (!newSeriesData[i].color || newSeriesData[i].color.length == 0) {
                newSeriesData[i].color = colors_new[i];
            }
        }
    }

    newSeriesData.sort(SortByOrder);
    for (var i = 0; i < newSeriesData.length; i++) {
        table.jqGrid('addRowData', i + 1, newSeriesData[i]);
    }
    outerLayout.sizePane("south", $('#gbox_listdt').height())
}
function strEndsWith(str, suffix) {
     return str.match(suffix+"$")==suffix;
}

/**
 * @return {number}
 */
function SortByOrder(a, b){
  var aOrder = a.order;
  var bOrder = b.order;
  return ( aOrder -  bOrder );
}
/**
 * Construct a list of RGBA color hex represenations with the specified length
 * and format #RRGGBBAA, where the colors are spaced equally along the rainbow
 * spectrum.
 */
function rainbow(num) {
    if (1 > num) {
        return [];
    }
    if (1 == num) {
        return ["#FF0000"];
    }

    var listRet = [];
    var dblInc = 1.0 / (num - 1);
    var dblVal = 0;
    for (var i = 0; i < num; i++, dblVal += dblInc) {
        listRet.push("#" + interpolateColor(dblVal));
    }
    return listRet;
}
/**
 * Create a hex representation of the specified "rainbow" color along the
 * spectrum from 0 (red, FF0000) to 1 (violet, FF00FF).
 */
function interpolateColor(rel){
	if     ( rel < 0.0 ) { return "FF0000"; }
	else if( rel > 1.0 ) { return "FF00FF"; }

	var min = 0;
	var max = 1;

	switch( Math.floor(rel/.2) ){
		case 0:					return hex(max) + hex(max*(min + (1-min)*(rel/.2))) + hex(min);
		case 1:	rel -= .2;		return hex(min + max*(1-min)*(1 - rel/.2)) + hex(max) + hex(min);
		case 2:	rel -= .4;		return hex(min) + hex(max) + hex(max*(min + (1-min)*(rel/.2)));
		case 3:	rel -= .6;		return hex(min) + hex(max*(1-min)*(1 - rel/.2)) + hex(max);
		case 4:	rel -= .8;		return hex(max*(min + (1-min)*(rel/.2))*.5) + hex(min) + hex(max);
		default:				return hex(max*.5) + hex(min) + hex(max);
	}
}
/**
 * Create the two character hexadecimal representation of specified value,
 * multiplied by 255. The intended use is to create an RGB representation with
 * 8-bit color depth.
 */
function hex(val){
	var strRet = Math.round(val * 255).toString(16).toUpperCase();
	while( 2 > strRet.length ){ strRet = "0" + strRet; }
	return strRet;
}

function permuteSeriesNew(map, intIndex) {

    var keys = Object.keys(map);
    if (1 > keys.length) {
        return [];
    }
    var strVar = keys[intIndex];
    var listVal = map[strVar];

    //  if the index has reached the end of the list, return the selected values
    //  from the last control
    if (keys.length == intIndex + 1) {

        return listVal;
    }

    //  otherwise, get the list for the next fcst_var and build upon it
    var listValNext = permuteSeriesNew(map, intIndex + 1);
    if (1 > listVal.length) {
        return listValNext;
    }

    var listRet = [];
    for (var j = 0; j < listValNext.length; j++) {
        for (var i = 0; i < listVal.length; i++) {

            listRet.push(listVal[i] + " " + listValNext[j]);
        }
    }
    return listRet;
}


function permuteSeries(series_var_indexes, intIndex, y_axis) {

    if (series_var_indexes.length < 1) {
        return [];
    }
    var listVal=[];
    var listSelectedBoxes;
    try{
        listSelectedBoxes = $("#series_var_val_"+ y_axis + "_" + series_var_indexes[intIndex]).multiselect("getChecked");
    } catch (err){
        listSelectedBoxes=[];
    }
    for(var i=0; i< listSelectedBoxes.length; i++){
        listVal.push(listSelectedBoxes[i].value);
    }
// if the index has reached the end of the list, return the selected values
// from the last control
    if (series_var_indexes.length == intIndex + 1) {

        return listVal;
    }
// otherwise, get the list for the next fcst_var and build upon it
    var listValNext = permuteSeries(series_var_indexes, intIndex + 1, y_axis);
    if (listVal.length < 1) {
        return listValNext;
    }
    var listRet = [];
    for (var j = 0; j < listValNext.length; j++) {
        for (var i = 0; i < listVal.length; i++) {
            listRet.push(listVal[i] + " " + listValNext[j]);
        }
    }
    return listRet;
}
function populateSecondSelect(yAxis,seriesNames) {
    var select1 = $('#series1Y' + yAxis);
    if (select1.children().length > 0) {
        var selectedSeries = select1.val();
        var variableStatArray = selectedSeries.split(" ");
        var variableStat;
        if (variableStatArray.length > 2) {
            variableStat = variableStatArray[variableStatArray.length - 2] + " " + variableStatArray[variableStatArray.length - 1];
        }
        var select2 = $('#series2Y' + yAxis);
        select2.empty();
        for (var i = 0; i < seriesNames.length; i++) {
            if (seriesNames[i].endsWith(variableStat) && selectedSeries != seriesNames[i]) {
                select2.append($("<option></option>")
                        .attr("value", seriesNames[i])
                        .text(seriesNames[i]));

            }
        }
    }
}
function createNewDiffSeriesName(yAxis) {
    var val1 = $('#series1Y' + yAxis).val();
    var val2 = $('#series2Y' + yAxis).val();
    if(val1 && val2){
        $('#newDiffSeriesName').text('DIFF ( "' + val1 + '"-"' + val2 + '" )');
    }else{
        $('#newDiffSeriesName').text("N/A");
    }
}

function changeYAxis(yAxis) {
    if (yAxis == 2) {
        $('#series1Y2').removeAttr('disabled');
        $('#series2Y2').removeAttr('disabled');
        $('#series1Y1').attr("disabled", "disabled");
        $('#series2Y1').attr("disabled", "disabled");
        createNewDiffSeriesName(2);
    } else {
        $('#series1Y2').attr("disabled", "disabled");
        $('#series2Y2').attr("disabled", "disabled");
        $('#series1Y1').removeAttr('disabled');
        $('#series2Y1').removeAttr('disabled');
        createNewDiffSeriesName(1);
    }
}

function sendXml() {
    var xml;
    var template;
    var result = $('<request />');
    result.append($('<db_con />').text($("#database").multiselect("getChecked").val()));
    var plot = $('<plot />');
    if (currentTab == 'Series') {
        template = $('<template>series_plot.R_tmpl</template>');
        plot.append(template);
        plot = createXMLSeries(plot);
        plot= createXMLCommon(plot);
    } else if (currentTab == 'Box') {
        template = $('<template>box_plot.R_tmpl</template>');
        plot.append(template);
        plot = createXMLBox(plot);
    } else if (currentTab == 'Bar') {
        template = $('<template>bar_plot.R_tmpl</template>');
        plot.append(template);
        plot = createXMLBox(plot);
    } else if (currentTab == 'Rhist') {
        template = $('<template>rhist.R_tmpl</template>');
        plot.append(template);
        plot = createXMLRhist(plot);
    } else if (currentTab == 'Phist') {
        template = $('<template>phist.R_tmpl</template>');
        plot.append(template);
        plot = createXMLPhist(plot);
    } else if (currentTab == 'Roc') {
        template = $('<template>roc.R_tmpl</template>');
        plot.append(template);
        plot = createXMLRoc(plot);
    } else if (currentTab == 'Rely') {
        template = $('<template>rely.R_tmpl</template>');
        plot.append(template);
        plot = createXMLRely(plot);
    } else if (currentTab == 'Ens_ss') {
        template = $('<template>ens_ss.R_tmpl</template>');
        plot.append(template);
        plot = createXMLEns(plot);
    }
    result.append(plot);
    xml= $('<root />').append(result).html();

    $.ajax({
        url: 'servlet',
        processData: false,
        contentType: 'application/json',
        type: "POST",  // type should be POST
        data: xml, // send the string directly
        success: function (response) {
            var xmlDoc;
            try {
                xmlDoc = $.parseXML(response);
                var xml = $(xmlDoc);
                var plot = xml.find("plot");
                var error = xml.find("error");
                if (error.length == 0) {
                    updateResult(plot.text());
                    var r_error = xml.find("r_error");
                    if (r_error && r_error.length > 0) {
                        updateResult(plot.text());
                        $("#r_error_message_text").empty().text(r_error.text());
                        $("#r_error_message").dialog("open");
                    }
                } else {
                    updateResult(plot.text());
                    $("#error_message_text").empty().text(xml.find("error").text());
                    $("#error_message").dialog("open");
                }
            } catch (err) {
                $("#error_message_text").empty().text(response);
                $("#error_message").dialog("open");
            }
        },
        error: function (response) {
            alert(response);
        }
    });
}

function createXMLEns(plot){

    plot.append(createSeriesElementForAxis(1, series_var_y1_indexes));
    plot= createXMLPlotFix(plot);
    plot.append($('<ensss_pts />').text($("#ensss_pts").val()));
    plot.append($('<ensss_pts_disp />').text($("input:radio[name='ensss_pts_disp']:checked").val()));
    plot= createXMLCommon(plot);
    return plot;
}

function createXMLRoc(plot){
    try {
        $("#listdt").saveCell(lastSelRow, lastSelCol);
    } catch (err) {
        console.log("Can't save row " + lastSelRow);
    }
    plot.append(createSeriesElementForAxis(1, series_var_y1_indexes));
    plot = createXMLPlotFix(plot);
    var roc_calc = $('<roc_calc />');
    var roc_type = $("input:radio[name='roc_type']:checked").val();
    if(roc_type == "pct"){
        roc_calc.append($('<roc_pct />').text("true"));
        roc_calc.append($('<roc_ctc />').text("false"));
    }else{
        roc_calc.append($('<roc_pct />').text("false"));
        roc_calc.append($('<roc_ctc />').text("true"));
    }
    plot.append(roc_calc);
    plot= createXMLCommon(plot);
    return plot;
}

function createXMLPhist(plot){
    try {
        $("#listdt").saveCell(lastSelRow, lastSelCol);
    } catch (err) {
        console.log("Can't save row " + lastSelRow);
    }
    plot.append(createSeriesElementForAxis(1, series_var_y1_indexes));
    plot = createXMLPlotFix(plot);
    plot.append($('<normalized_histogram />').text($("input:radio[name='normalized_histogram']:checked").val()));
    plot = createXMLCommon(plot);
    return plot;
}
function createXMLRhist(plot) {
    try {
        $("#listdt").saveCell(lastSelRow, lastSelCol);
    } catch (err) {
        console.log("Can't save row " + lastSelRow);
    }
    plot.append(createSeriesElementForAxis(1, series_var_y1_indexes));
    plot = createXMLPlotFix(plot);
    plot.append($('<normalized_histogram />').text($("input:radio[name='normalized_histogram']:checked").val()));
    plot = createXMLCommon(plot);
    return plot;
}

function createXMLRely(plot){
    try {
           $("#listdt").saveCell(lastSelRow, lastSelCol);
       } catch (err) {
           console.log("Can't save row " + lastSelRow);
       }
       plot.append(createSeriesElementForAxis(1, series_var_y1_indexes));
    plot= createXMLPlotFix(plot);
    plot.append($('<rely_event_hist />').text($("input:radio[name='rely_event_hist']:checked").val()));
    plot= createXMLCommon(plot);
    return plot;
}
function createXMLBox(plot) {
    plot= createXMLSeries(plot);
    plot= createXMLCommon(plot);
    plot.append($('<box_pts />').text($('#box_pts').is(':checked')));
    plot.append($('<box_outline />').text($('#box_outline').is(':checked')));
    plot.append($('<box_notch />').text($('#box_notch').is(':checked')));
    plot.append($('<box_avg />').text($('#box_avg').is(':checked')));
    plot.append($('<box_boxwex />').text($('#box_boxwex').val()));
    return plot;
}

function createXMLPlotFix(plot){
    var plot_fix = $('<plot_fix />');
        for (var i = 0; i < fixed_var_indexes.length; i++) {
            var field = $('<field />').attr("name", $("#fixed_var_" + fixed_var_indexes[i]).val());
            var set = $('<set />').attr("name", $("#fixed_var_" + fixed_var_indexes[i]).val() + "_" + i);
            var valArr = $("#fixed_var_val_" + fixed_var_indexes[i]).val();
            if (valArr) {
                for (var j = 0; j < valArr.length; j++) {
                    set.append($('<val />').text(valArr[j]));
                }
                field.append(set);
                plot_fix.append(field);
            }
        }
        plot.append(plot_fix);
    return plot;
}


function createXMLSeries(plot) {
    try{
        $("#listdt").saveCell(lastSelRow,lastSelCol);
    } catch (err){
        console.log("Can't save row " + lastSelRow);
    }
    var selected_mode = $("#plot_data").multiselect("getChecked").val();


    var dep = $('<dep />');
    dep.append(createDepElementForAxis(1, fcst_var_y1_indexes, selected_mode));
    dep.append(createDepElementForAxis(2, fcst_var_y2_indexes, selected_mode));
    plot.append(dep);
    plot.append(createSeriesElementForAxis(1, series_var_y1_indexes));
    plot.append(createSeriesElementForAxis(2, series_var_y2_indexes));

    plot= createXMLPlotFix(plot);
    plot.append($('<plot_cond />').text($('#txtPlotCond').val()));
    var indep = $('<indep />').attr("name", $('#indy_var').val());
    var indy_var_val = $('[name="multiselect_indy_var_val"]');
    if ($("#indy_var_val").multiselect("getChecked").length > 0) {
        for (var i = 0; i < indy_var_val.length; i++) {
            var jqObject = $(indy_var_val[i]);
            if (jqObject.prop('checked')) {
                var id = jqObject.attr("id");
                indep.append($('<val />').attr("label", $('#' + id + '_label').val()).attr("plot_val", $('#' + id + '_plot_val').val()).text(jqObject.val()));
            }
        }
    } else {
        var start = moment($("#date_period_start").val(), 'YYYY-MM-DD HH:mm:ss');//2007-08-15 12:00:00
        var end = moment($("#date_period_end").val(), 'YYYY-MM-DD HH:mm:ss');
        var by = $("#date_period_by").val().trim();
        var unit = $("#date_period_by_unit").val();
        var dates = $(previousIndVarValResponse).find("val");
        if(by.length == 0) {
            for (var i = 0; i < dates.length; i++) {
                var t = moment($(dates[i]).text(), 'YYYY-MM-DD HH:mm:ss');
                if (t.isSame(start) || t.isSame(end) || t.isBetween(start, end)) {
                    indep.append($('<val />').attr("label", $(dates[i]).text()).attr("plot_val", "").text($(dates[i]).text()));
                }
            }
        } else {
            by = parseInt(by);
            if (unit == "days") {
                by = by * 24;
            }
            var current_date = start.clone();
            while (current_date <= end) {
                var current_date_str = current_date.format('YYYY-MM-DD HH:mm:ss');
                var isFound = false;
                for (var i = 0; i < dates.length; i++) {
                    if($(dates[i]).text() == current_date_str){
                        isFound=true;
                        break;
                    }
                }
                if(isFound){
                    indep.append($('<val />').attr("label", current_date_str).attr("plot_val", "").text(current_date_str));
                }
                current_date.add(by, 'hour');
            }
        }
    }
    plot.append(indep);
    var statistics_val = $("input:radio[name='statistics']:checked").val();

    if(statistics_val == 'aggregation_statistics'){
        var agg_stat = $('<agg_stat />');
        agg_stat.append($('<agg_ctc />').text($('#agg_ctc').is(':checked')));
        agg_stat.append($('<agg_sl1l2 />').text($('#agg_sl1l2').is(':checked')));
        agg_stat.append($('<agg_pct />').text($('#agg_pct').is(':checked')));
        agg_stat.append($('<agg_nbrcnt />').text($('#agg_nbrcnt').is(':checked')));
        agg_stat.append($('<boot_repl />').text($('#boot_repl').val()));
        agg_stat.append($('<boot_ci />').text($('#boot_ci').val()));
        agg_stat.append($('<eveq_dis />').text($('#eveq_dis').is(':checked')));
        agg_stat.append($('<cache_agg_stat />').text($('#cache_agg_stat').is(':checked')));
        plot.append(agg_stat);
    }else if(statistics_val == 'calculations_statistics'){
        var calc_stat = $('<calc_stat />');
        calc_stat.append($('<calc_ctc />').text($('#calc_ctc').is(':checked')));
        calc_stat.append($('<calc_sl1l2 />').text($('#calc_sl1l2').is(':checked')));
        plot.append(calc_stat);
    }
    plot.append($('<plot_stat />').text($('#plot_stat').val()));
    return plot;


}
function createXMLCommon(plot) {
    var tmpl = $('<tmpl />');
    tmpl.append($('<title />').text($('#plot_title').val()));
    tmpl.append($('<x_label />').text($('#x_label_title').val()));
    tmpl.append($('<y1_label />').text($('#y1_label_title').val()));
    tmpl.append($('<y2_label />').text($('#y2_label_title').val()));
    tmpl.append($('<caption />').text($('#caption').val()));
    tmpl.append($('<file_name />').text($('#file_name').val()));

    var seriesDiffY1List = [];
    if (seriesDiffY1.length > 0) {
        for (var i = 0; i < seriesDiffY1.length; i++) {
            seriesDiffY1List.push('c("' + seriesDiffY1[i].split(",")[0] + '","' + seriesDiffY1[i].split(",")[1] + '")');
        }
    }
    var seriesDiffY2List = [];
    if (seriesDiffY2.length > 0) {
        for (var i = 0; i < seriesDiffY2.length; i++) {
            seriesDiffY2List.push('c("' + seriesDiffY2[i].split(",")[0] + '","' + seriesDiffY2[i].split(",")[1] + '")');
        }
    }
    tmpl.append($('<listDiffSeries1 />').text("list(" + seriesDiffY1List.join() + ")"));
    tmpl.append($('<listDiffSeries2 />').text("list(" + seriesDiffY2List.join() + ")"));
    plot.append(tmpl);
    plot.append($('<event_equal />').text($('#event_equal').is(':checked')));
    plot.append($('<event_equal_m />').text($('#event_equal_m').is(':checked')));
    plot.append($('<vert_plot />').text($('#vert_plot').is(':checked')));
    plot.append($('<x_reverse />').text($('#x_reverse').is(':checked')));
    plot.append($('<num_stats />').text($('#num_stats').is(':checked')));
    plot.append($('<indy1_stag />').text($('#indy1_stag').is(':checked')));
    plot.append($('<indy2_stag />').text($('#indy2_stag').is(':checked')));
    plot.append($('<grid_on />').text($('#grid_on').is(':checked')));
    plot.append($('<sync_axes />').text($('#sync_axes').is(':checked')));
    plot.append($('<dump_points1 />').text($('#dump_points1').is(':checked')));
    plot.append($('<dump_points2 />').text($('#dump_points2').is(':checked')));
    plot.append($('<log_y1 />').text($('#log_y1').is(':checked')));
    plot.append($('<log_y2 />').text($('#log_y2').is(':checked')));
    plot.append($('<varianceInflationFactor />').text($('#varianceInflationFactor').is(':checked')));
    plot.append($('<plot_type />').text($('#plot_type').val()));
    plot.append($('<plot_height />').text($('#plot_height').val()));
    plot.append($('<plot_width />').text($('#plot_width').val()));
    plot.append($('<plot_res />').text($('#plot_res').val()));
    plot.append($('<plot_units />').text($('#plot_units').val()));
    var mar_bottom = $("#mar_bottom").val().trim();
    var mar_left = $("#mar_left").val().trim();
    var mar_top = $("#mar_top").val().trim();
    var mar_right = $("#mar_right").val().trim();
    if (mar_bottom.length == 0) {
        mar_bottom = 0;
    }
    if (mar_left.length == 0) {
        mar_left = 0;
    }
    if (mar_top.length == 0) {
        mar_top = 0;
    }
    if (mar_right.length == 0) {
        mar_right = 0;
    }
    plot.append($('<mar />').text("c(" + mar_bottom + "," + mar_left + "," + mar_top + "," + mar_right + ")"));

    var mgp_title = $("#mgp_title").val().trim();
    var mgp_labels = $("#mgp_labels").val().trim();
    var mgp_line = $("#mgp_line").val().trim();
    if (mgp_title.length == 0) {
        mgp_title = 0;
    }
    if (mgp_labels.length == 0) {
        mgp_labels = 0;
    }
    if (mgp_line.length == 0) {
        mgp_line = 0;
    }
    plot.append($('<mgp />').text("c(" + mgp_title + "," + mgp_labels + "," + mgp_line + ")"));
    plot.append($('<cex />').text($('#cex').val()));
    plot.append($('<title_weight />').text($('#title_weight').val()));
    plot.append($('<title_size />').text($('#title_size').val()));
    plot.append($('<title_offset />').text($('#title_offset').val()));
    plot.append($('<title_align />').text($('#title_align').val()));
    plot.append($('<xtlab_orient />').text($('#xtlab_orient').val()));
    plot.append($('<xtlab_perp />').text($('#xtlab_perp').val()));
    plot.append($('<xtlab_horiz />').text($('#xtlab_horiz').val()));
    plot.append($('<xtlab_freq />').text($('#xtlab_freq').val()));
    plot.append($('<xtlab_size />').text($('#xtlab_size').val()));
    plot.append($('<xlab_weight />').text($('#xlab_weight').val()));
    plot.append($('<xlab_size />').text($('#xlab_size').val()));
    plot.append($('<xlab_offset />').text($('#xlab_offset').val()));
    plot.append($('<xlab_align />').text($('#xlab_align').val()));
    plot.append($('<ytlab_orient />').text($('#ytlab_orient').val()));
    plot.append($('<ytlab_perp />').text($('#ytlab_perp').val()));
    plot.append($('<ytlab_horiz />').text($('#ytlab_horiz').val()));
    plot.append($('<ytlab_size />').text($('#ytlab_size').val()));
    plot.append($('<ylab_weight />').text($('#ylab_weight').val()));
    plot.append($('<ylab_size />').text($('#ylab_size').val()));
    plot.append($('<ylab_offset />').text($('#ylab_offset').val()));
    plot.append($('<ylab_align />').text($('#ylab_align').val()));
    plot.append($('<grid_lty />').text($('#grid_lty').val()));

    var grid_col = $('#grid_col').val();
    if (grid_col.startsWith("#")) {
        plot.append($('<grid_col />').text(grid_col));
    } else {
        plot.append($('<grid_col />').text("#" + grid_col));
    }
    plot.append($('<grid_lwd />').text($('#grid_lwd').val()));
    plot.append($('<grid_x />').text($('#grid_x').val()));
    plot.append($('<x2tlab_orient />').text($('#x2tlab_orient').val()));
    plot.append($('<x2tlab_perp />').text($('#x2tlab_perp').val()));
    plot.append($('<x2tlab_horiz />').text($('#x2tlab_horiz').val()));
    plot.append($('<x2tlab_size />').text($('#x2tlab_size').val()));
    plot.append($('<x2lab_size />').text($('#x2lab_size').val()));
    plot.append($('<x2lab_offset />').text($('#x2lab_offset').val()));
    plot.append($('<x2lab_align />').text($('#x2lab_align').val()));
    plot.append($('<y2tlab_orient />').text($('#y2tlab_orient').val()));
    plot.append($('<y2tlab_perp />').text($('#y2tlab_perp').val()));
    plot.append($('<y2tlab_horiz />').text($('#y2tlab_horiz').val()));
    plot.append($('<y2tlab_size />').text($('#y2tlab_size').val()));
    plot.append($('<y2lab_size />').text($('#y2lab_size').val()));
    plot.append($('<y2lab_offset />').text($('#y2lab_offset').val()));
    plot.append($('<y2lab_align />').text($('#y2lab_align').val()));
    plot.append($('<legend_box />').text($('#legend_box').val()));

    if ($('#legend_inset_min').val().trim().length == 0 || $('#legend_inset_max').val().trim().length == 0) {
        plot.append($('<legend_inset />').text("c()"));
    } else {
        plot.append($('<legend_inset />').text("c(" + $('#legend_inset_min').val() + "," + $('#legend_inset_max').val() + ")"));
    }
    plot.append($('<legend_ncol />').text($('#legend_ncol').val()));
    plot.append($('<legend_size />').text($('#legend_size').val()));
    plot.append($('<caption_weight />').text($('#caption_weight').val()));

    var caption_col = $('#caption_col').val();
    if (caption_col.startsWith("#")) {
        plot.append($('<caption_col />').text(caption_col));
    } else {
        plot.append($('<caption_col />').text("#" + caption_col));
    }
    plot.append($('<caption_size />').text($('#caption_size').val()));
    plot.append($('<caption_offset />').text($('#caption_offset').val()));
    plot.append($('<caption_align />').text($('#caption_align').val()));
    plot.append($('<ci_alpha />').text($('#ci_alpha').val()));


    var allSeries = sortSeries();
    var ciArr = [], dispArr = [], colorsArr = [], pchArr = [], typeArr = [], ltyArr = [], lwdArr = [], conArr = [], orderArr = [], legendArr = [], showSignArr = [];
    for (var i = 0; i < allSeries.length; i++) {
        ciArr.push('"' + allSeries[i].plot_ci + '"');
        if (allSeries[i].hide == "No") {
            dispArr.push("TRUE");
        } else {
            dispArr.push("FALSE");
        }
        if (allSeries[i].show_signif == "No") {
            showSignArr.push("FALSE");
        } else {
            showSignArr.push("TRUE");
        }
        colorsArr.push('"#' + allSeries[i].color + 'FF"');
        pchArr.push(allSeries[i].pch);
        typeArr.push('"' + allSeries[i].type + '"');
        ltyArr.push(allSeries[i].lty);
        lwdArr.push(allSeries[i].lwd);
        conArr.push(allSeries[i].con_series);
        orderArr.push(allSeries[i].order);
        legendArr.push('"' + allSeries[i].legend + '"');
    }
    plot.append($('<plot_ci />').text("c(" + ciArr.join() + ")"));
    plot.append($('<show_signif />').text("c(" + showSignArr.join() + ")"));
    plot.append($('<plot_disp />').text("c(" + dispArr.join() + ")"));
    plot.append($('<colors />').text("c(" + colorsArr.join() + ")"));
    plot.append($('<pch />').text("c(" + pchArr.join() + ")"));
    plot.append($('<type />').text("c(" + typeArr.join() + ")"));
    plot.append($('<lty />').text("c(" + ltyArr.join() + ")"));
    plot.append($('<lwd />').text("c(" + lwdArr.join() + ")"));
    plot.append($('<con_series />').text("c(" + conArr.join() + ")"));
    plot.append($('<order_series />').text("c(" + orderArr.join() + ")"));
    plot.append($('<plot_cmd />').text($('#plot_cmd').val()));
    plot.append($('<legend />').text("c(" + legendArr.join() + ")"));


    if ($('#y1_lim_min').val().trim().length == 0 || $('#y1_lim_max').val().trim().length == 0) {
        plot.append($('<y1_lim />').text("c()"));
    } else {
        plot.append($('<y1_lim />').text("c(" + $('#y1_lim_min').val() + "," + $('#y1_lim_max').val() + ")"));
    }
    plot.append($('<y1_bufr />').text($('#y1_bufr').val()));
    if ($('#y2_lim_min').val().trim().length == 0 || $('#y2_lim_max').val().trim().length == 0) {
        plot.append($('<y2_lim />').text("c()"));
    } else {
        plot.append($('<y2_lim />').text("c(" + $('#y2_lim_min').val() + "," + $('#y2_lim_max').val() + ")"));
    }
    plot.append($('<y2_bufr />').text($('#y2_bufr').val()));

    return plot;
}
function sortSeries () {
    var allSeries = $("#listdt").jqGrid('getRowData');
    allSeries = allSeries.sort(function(a, b) {
         return (parseInt(a.id) > parseInt(b.id)) ? 1 : ((parseInt(a.id) < parseInt(b.id)) ? -1 : 0);

    });
    return allSeries;
}
function createSeriesElementForAxis(y_axis, series_var_indexes){
    var series = $('<series'+ y_axis +' />');
        for (var i = 0; i < series_var_indexes.length; i++) {
        //for (var i = series_var_indexes.length-1; i >= 0; i--) {
            var field = $('<field />').attr("name", $("#series_var_y"+ y_axis +"_" + series_var_indexes[i]).val());
            var isGroup = $("#group_series_var_y"+ y_axis +"_" + series_var_indexes[i]).is(':checked');
            var valArr = $("#series_var_val_y"+ y_axis +"_" + series_var_indexes[i]).val();
            if(!valArr || valArr == null){
                valArr=[];
            }
            if (isGroup) {
                valArr = [valArr.join()];
            }
            for (var j = 0; j < valArr.length; j++) {
                field.append($('<val />').text(valArr[j]));
            }
            series.append(field);
        }
    return series;
}

function createDepElementForAxis(y_axis, fcst_var_indexes,selected_mode){
    var depAxis = $('<dep'+ y_axis +' />');

    var forecast_var_to_stat_map = createMapForForecastVar(y_axis, fcst_var_indexes, selected_mode);
    for (var name in forecast_var_to_stat_map) {
        var fcst_var = $('<fcst_var />').attr("name", name);
        for(var i=0; i< forecast_var_to_stat_map[name].length; i++){
            fcst_var.append($('<stat />').text(forecast_var_to_stat_map[name][i]));
        }
        depAxis.append(fcst_var);
    }
    return depAxis;
}

function refreshHistory() {
    $('#history_content').empty();
    var result = $('<request />');
    var history=$('<history />');
    var isShowAll=false;
    if($("input:radio[name='show_history_choice']:checked").val() == 'show_history_all'){
        isShowAll=true;
    }
    history.append($('<show_all>'+isShowAll+ '</show_all>')) ;
    result.append(history);
    $.ajax({
        url: 'servlet',
        processData: false,
        type: "POST",
        dataType:'xml',
        data: $('<root />').append(result).html(), // send the string directly
        success: function (response) {
            var xml = $(response);
                var results = $(xml.find("results")[0]).find("file");
                for (var i = 0; i < results.length; i++) {
                    var name = $(results[i]).attr("name");
                    var success = $(results[i]).attr("success");
                    var color;
                    if(success == "true"){
                        color="#000000"
                    }else{
                        color="#B39A9A"
                    }
                    $('#history_content').append($('<tr/>').append($('<td/>').css("color",color ).text(name)).append($('<td/>').append($('<button />').attr("id", name).addClass('view-image').text("Image"))).append($('<td/>').append($('<button />').attr("id", name).addClass('load-image').text("XML"))));
                }
                $(".view-image").button({
                    icons: {
                        primary: "ui-icon-image"
                    },
                    text: false
                }).click(function () {
                        viewImage($(this).attr('id'));
                    });
                $(".load-image").button({
                    icons: {
                        primary: "ui-icon-document"
                    },
                    text: false
                }).click(function () {
                        $('#uploadLocalId').val($(this).attr('id'));
                            loadImage($(this).attr('id'));
                    });


        },
        error: function (response) {
            alert(response);
        }
    });
}

function loadImage(id) {
    boxID = boxID + 1;
    center = center + 20;
    top = top + 20;
    var dialog = $('<div>').attr("id", boxID+"dialog").dialog({
        height: 400,
        width: 'auto',
        resizable: true,
        closeOnEscape: true,
        autoOpen: false,
        focus: true,
        dialogClass: "dialog-box-" + boxID,
        title: id,
        position: { my: ("center+" + center + " top+" + top), at: "center top", of: window},
        close: function (e) {
            $(this).empty();
            $(this).dialog('destroy');
            center = center - 20;
            top = top - 20;
        },
        buttons: {
            "Load to Page": function () {
                $('#formUploadLocal').submit();
                $(this).dialog("close");
            },
            "Download to Disk": function () {
                var inputs = '';
                inputs += '<input type="hidden" name="plot" value="plot_' + id + '" />';
                inputs += '<input type="hidden" name="type" value="plot_xml_url" />';
                $('<form action="download" method="' + 'get' + '">' + inputs + '</form>').appendTo('body').submit().remove();
                $(this).dialog("close");
            }
        },
        create: function( event, ui ) {
            $.ajax({
                    async: false,
                    type: "GET",
                    url: "xml/plot_" + id + ".xml",
                    dataType: "xml",
                    success: function (data) {
                        var xmlString;
                        if (jQuery.browser == "msie") {
                            xmlString = data.xml;
                        }
                        else {
                            xmlString = (new XMLSerializer()).serializeToString(data);
                        }
                        try{
                            $("#" + boxID+"dialog").append(formatXml(xmlString).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/ /g, '&nbsp;').replace(/\n/g, '<br />'));
                        } catch (e){
                            console.log(e + " " + "can't append xml");
                        }
                    }
                });
        }
    });

    dialog.dialog("open");
}

function formatXml(xml) {
    var formatted = '';
    var reg = /(>)(<)(\/*)/g;
    xml = xml.replace(reg, '$1\r\n$2$3');
    var pad = 0;
    jQuery.each(xml.split('\r\n'), function (index, node) {
        var indent = 0;
        if (node.match(/.+<\/\w[^>]*>$/)) {
            indent = 0;
        } else if (node.match(/^<\/\w/)) {
            if (pad != 0) {
                pad -= 1;
            }
        } else if (node.match(/^<\w[^>]*[^\/]>.*$/)) {
            indent = 1;
        } else {
            indent = 0;
        }

        var padding = '';
        for (var i = 0; i < pad; i++) {
            padding += ' ';
        }

        formatted += padding + node + '\r\n';
        pad += indent;
    });

    return formatted;
}
function resetFormatting() {
    $('#file_name').val("");
    $('#plot_title').val("test title");
    $('#x_label_title').val("test x_label");
    $('#y1_label_title').val("test y_label");
    $('#y2_label_title').val("");
    $('#caption').val("");

    $("#event_equal").prop('checked', false);
    $("#event_equal_m").prop('checked', false);
    $("#vert_plot").prop('checked', false);
    $("#x_reverse").prop('checked', false);
    $("#num_stats").prop('checked', false);
    $("#grid_on").prop('checked', true);
    $("#sync_axes").prop('checked', false);
    $("#dump_points1").prop('checked', false);
    $("#dump_points2").prop('checked', false);
    $("#indy1_stag").prop('checked', false);
    $("#indy2_stag").prop('checked', false);
    $("#varianceInflationFactor").prop('checked', true);
    $("#ci_alpha").val("0.05");

    $("#plot_type").val("png16m");
    $("#plot_height").val("8.5");
    $("#plot_width").val("11");
    $("#plot_units").val("in");
    $("#cex").val("1");
    $("#plot_res").val("72");
    $("#mar_bottom").val("8");
    $("#mar_left").val("4");
    $("#mar_top").val("5");
    $("#mar_right").val("4");

    $("#mgp_title").val("1");
    $("#mgp_labels").val("1");
    $("#mgp_line").val("0");
    $("#title_align").val("0.5");
    $("#title_offset").val("-2");
    $("#title_size").val("1.4");
    $("#title_weight").val("2");
    $("#grid_lty").val("3");
    $("#grid_lwd").val("1");
    $("#grid_x").val("listX");
    $("#plot_cmd").val("");

    $("#xlab_align").val("0.5");
    $("#xlab_offset").val("2");
    $("#xlab_size").val("1");
    $("#xlab_weight").val("1");
    $("#xtlab_horiz").val("0.5");
    $("#xtlab_perp").val("-0.75");
    $("#xtlab_size").val("1");
    $("#xtlab_freq").val("0");
    $("#xtlab_orient").val("1");

    $("#x2lab_align").val("0.5");
    $("#x2lab_offset").val("-0.5");
    $("#x2lab_size").val("0.8");
    $("#x2lab_weight").val("1");
    $("#x2tlab_horiz").val("0.5");
    $("#x2tlab_perp").val("1");
    $("#x2tlab_size").val("0.8");
    $("#x2tlab_orient").val("1");

    $("#ylab_align").val("0.5");
    $("#ylab_offset").val("-2");
    $("#ylab_size").val("1");
    $("#ylab_weight").val("1");
    $("#ytlab_horiz").val("0.5");
    $("#ytlab_perp").val("0.5");
    $("#ytlab_size").val("1");
    $("#ytlab_orient").val("1");
    $("#y1_lim").val("");
    $("#y1_bufr").val("0.04");


    $("#y2lab_align").val("0.5");
    $("#y2lab_offset").val("1");
    $("#y2lab_size").val("1");
    $("#y2lab_weight").val("1");
    $("#y2tlab_horiz").val("0.5");
    $("#y2tlab_perp").val("0.5");
    $("#y2tlab_size").val("1");
    $("#y2tlab_orient").val("1");
    $("#y2_lim_min").val("0");
    $("#y2_lim_max").val("1");
    $("#y2_bufr").val("0");

    $("#legend_size").val("0.8");
    $("#legend_inset_min").val("0");
    $("#legend_inset_max").val(" -.25");
    $("#legend_box").val("o");
    $("#legend_ncol").val("3");
    $("#caption_align").val("0");
    $("#caption_offset").val("3");
    $("#caption_size").val("0.8");
    $("#caption_weight").val("1");


}

function removeFixedVarRhist(id) {
    var id_array = id.split("_");
    var index = id_array[id_array.length - 1];
    //destroy selects
    var select = $("#fixed_var_" + index);
    select.multiselect("destroy");
    $("#fixed_var_val_" + index).multiselect("destroy");

    var index_of_removing_el = jQuery.inArray(parseInt(index), fixed_var_indexes);
    if (index_of_removing_el > -1) {
        fixed_var_indexes.splice(index_of_removing_el, 1);
        fixVarValResponse[parseInt(index)] = null;
    }
    if (fixed_var_indexes.length == 1) {
        $("#remove_fixed_var_" + fixed_var_indexes[0] ).button( {disabled: true} );
    }
    select.parent().parent().remove();
}

function removeFixedVarSeries(id) {
    var id_array = id.split("_");
    var index = id_array[id_array.length - 1];
    //destroy selects
    $("#fixed_var_" + index).multiselect("destroy");
    $("#fixed_var_val_" + index).multiselect("destroy");
    $("#fixed_var_val_date_period_" + index).dialog("destroy");
    $("#fixed_var_val_date_period_" + index).remove();
    $("#fixed_var_val_date_period_button_" + index).button("destroy");

    var index_of_removing_el = jQuery.inArray(parseInt(index), fixed_var_indexes);
    if (index_of_removing_el > -1) {
        fixed_var_indexes.splice(index_of_removing_el, 1);
    }
    if (fixed_var_indexes.length > 0) {
        $("#fixed_var_" + index).parent().parent().remove();
    } else {
        $('#fixed_var_table').css("display", "none");
    }

}



function addFixedVariableRhist() {
    var last_index = fixed_var_indexes[fixed_var_indexes.length - 1];
    if (fixed_var_indexes.length == 1) {
        $("#remove_fixed_var_" + last_index ).button( {disabled: false} );
    }
    fixed_var_indexes.push(last_index + 1);

    var fixed_var, remove_var, fixed_var_val,fixed_var_val_date_period_button, dialog;

    fixed_var = $("#fixed_var_" + last_index).clone(false);
    fixed_var.attr("id", 'fixed_var_' + (last_index + 1));
    fixed_var.css("display", '');


    remove_var = $("#remove_fixed_var_" + last_index).clone(true)
            .attr("id", 'remove_fixed_var_' + (last_index + 1));

    fixed_var_val = $("#fixed_var_val_" + last_index).clone(false);
    fixed_var_val.attr("id", 'fixed_var_val_' + (last_index + 1));
    fixed_var_val.css("display", '');
    fixed_var.val(fixed_var.find(" option:first").val());

    fixed_var_val_date_period_button = $("<button>", {
        text: 'Select multiple options',
        id: 'fixed_var_val_date_period_button_' + (last_index + 1)
    }).css('display', 'none');

    dialog = $("#fixed_var_val_date_period_" + (last_index )).clone(false).appendTo( "body" );
    dialog.prop("id", "fixed_var_val_date_period_" + (last_index + 1));
    dialog.find("#fixed_var_val_date_period_start_" + (last_index )).prop('id', 'fixed_var_val_date_period_start_' + (last_index + 1));
    dialog.find("#fixed_var_val_date_period_end_" + (last_index )).prop('id', 'fixed_var_val_date_period_end_' + (last_index + 1));
    dialog.find("#fixed_var_val_date_period_by_" + (last_index )).prop('id', 'fixed_var_val_date_period_by_' + (last_index + 1));
    dialog.find("#fixed_var_val_date_period_by_unit_" + (last_index )).prop('id', 'fixed_var_val_date_period_by_unit_' + (last_index + 1));


    $('#fixed_var_table').append($('<tr>').append($('<td>').append(remove_var)).append($('<td>').append(fixed_var)).append($('<td>').append(fixed_var_val)).append($('<td>').append(fixed_var_val_date_period_button)));

    createValDatePeriodDialog('fixed_var_val', (last_index + 1) );

    fixed_var_val.multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select value",
        position: {
            my: 'center center',
            at: 'right center'
        }
    });
    fixed_var.multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        height: 'auto',
        click: function (event, ui) {
            $('#fixed_var_val_date_period_start_' + (last_index + 1)).empty();
            $('#fixed_var_val_date_period_end_' + (last_index + 1)).empty();

            if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
                $("#fixed_var_val_date_period_button_" + (last_index + 1)).css("display", "block");
            } else {
                $("#fixed_var_val_date_period_button_" + (last_index + 1)).css("display", "none");
            }
            var id_array = this.id.split("_");
            updateFixedVarValHist(id_array[id_array.length - 1], []);
        }
    });
    fixed_var_val_date_period_button.button({
            icons: {
                primary: "ui-icon-check",
                secondary: "ui-icon-circlesmall-plus"
            },
            text: false
        }).click(function () {
            $("#fixed_var_val_date_period_" + (last_index + 1)).dialog("open");
        });
    updateFixedVarValHist((last_index + 1), []);
}

function addFixedVariableSeries() {
    var last_index;

    if (fixed_var_indexes.length > 0) {
        last_index = fixed_var_indexes[fixed_var_indexes.length - 1];
    } else {
        last_index = 0;
    }
    fixed_var_indexes.push(last_index + 1);

    var fixed_var, remove_var, fixed_var_val,fixed_var_val_date_period_button, dialog;
    if (last_index == 0) {
        $('#fixed_var_table').css("display", "");
        fixed_var = $("#fixed_var_" + (last_index + 1));
        $("#remove_fixed_var_" + (last_index + 1)).button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        });
        fixed_var_val = $("#fixed_var_val_" + (last_index + 1));
        fixed_var_val_date_period_button = $("#fixed_var_val_date_period_button_" + (last_index + 1));
        dialog = $("#fixed_var_val_date_period_" + (last_index + 1));

    } else {
        fixed_var = $("#fixed_var_" + last_index).clone(false);
        fixed_var.prop("id", 'fixed_var_' + (last_index + 1));
        fixed_var.css("display", '');
        fixed_var.val(fixed_var.find(" option:first").val());

    }

    if (last_index > 0) {
        remove_var = $("#remove_fixed_var_" + last_index).clone(true)
                .prop("id", 'remove_fixed_var_' + (last_index + 1));

        fixed_var_val = $("#fixed_var_val_" + last_index).clone(false);
        fixed_var_val.prop("id", 'fixed_var_val_' + (last_index + 1));
        fixed_var_val.css("display", '');
        fixed_var.val(fixed_var.find(" option:first").val());
        fixed_var_val_date_period_button = $("<button>",{
                text: 'Select multiple options',
                id: 'fixed_var_val_date_period_button_' + (last_index + 1)
            }).css('display', 'none');
        dialog = $("#fixed_var_val_date_period_" + (last_index )) .clone(false).appendTo( "body" );
        dialog.prop("id", "fixed_var_val_date_period_" + (last_index +1));
        dialog.find("#fixed_var_val_date_period_start_"+ (last_index )).prop('id', 'fixed_var_val_date_period_start_' + (last_index + 1));
        dialog.find("#fixed_var_val_date_period_end_"+ (last_index )).prop('id', 'fixed_var_val_date_period_end_' + (last_index + 1));
        dialog.find("#fixed_var_val_date_period_by_"+ (last_index )).prop('id', 'fixed_var_val_date_period_by_' + (last_index + 1));
        dialog.find("#fixed_var_val_date_period_by_unit_"+ (last_index )).prop('id', 'fixed_var_val_date_period_by_unit_' + (last_index + 1));

        $('#fixed_var_table').append($('<tr>').append($('<td>').append(remove_var)).append($('<td>').append(fixed_var)).append($('<td>').append(fixed_var_val)).append($('<td>').append(fixed_var_val_date_period_button)));
    }


    createValDatePeriodDialog('fixed_var_val', (last_index + 1) );

    fixed_var_val.multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select value",
        position: {
            my: 'center center',
            at: 'right center'
        }
    });
    fixed_var.multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        height: 'auto',
        click: function (event, ui) {
            $('#fixed_var_val_date_period_start_'+ (last_index + 1)).empty();
            $('#fixed_var_val_date_period_end_'+ (last_index + 1)).empty();

            if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
                $("#fixed_var_val_date_period_button_" + (last_index + 1)).css("display", "block");
            } else {
                $("#fixed_var_val_date_period_button_" + (last_index + 1)).css("display", "none");
            }
            var id_array = this.id.split("_");
            updateFixedVarValSeries(id_array[id_array.length - 1], []);
        }
    });
    fixed_var_val_date_period_button.button({
        icons: {
            primary: "ui-icon-check",
            secondary: "ui-icon-circlesmall-plus"
        },
        text: false
    }).click(function () {
        $("#fixed_var_val_date_period_" + (last_index + 1)).dialog("open");
    });

    updateFixedVarValSeries((last_index + 1), []);
}
function removeSeriesVarCommon(id) {
    var id_array = id.split("_");
    var index = id_array[id_array.length - 1];
    var y_axis = id_array[id_array.length - 2];
    var index_of_removing_el;
    //destroy selects
    try {
        $("#series_var_" + y_axis + "_" + index).multiselect("destroy");
    } catch (err) {
    }
    try {
        $("#series_var_val_" + y_axis + "_" + index).multiselect("destroy");
    } catch (err) {
    }
    if (y_axis == 'y1') {
        index_of_removing_el = jQuery.inArray(parseInt(index), series_var_y1_indexes);
        if (index_of_removing_el > -1) {
            series_var_y1_indexes.splice(index_of_removing_el, 1);
            seriesY1VarValResponse[parseInt(index)]=null;
        }

        if (series_var_y1_indexes.length > 0) {
            $("#series_var_" + y_axis + "_" + index).parent().parent().remove();
        } else {
            $('#series_var_table_' + y_axis).css("display", "none");
        }
    } else {
        index_of_removing_el = jQuery.inArray(parseInt(index), series_var_y2_indexes);
        if (index_of_removing_el > -1) {
            series_var_y2_indexes.splice(index_of_removing_el, 1);
            seriesY1VarValResponse[parseInt(index)]=null;
        }

        if (series_var_y2_indexes.length > 0) {
            $("#series_var_" + y_axis + "_" + index).parent().parent().remove();
        } else {
            $('#series_var_table_' + y_axis).css("display", "none");
        }
    }

}

function removeSeriesVarSeriesBox(id) {
    removeSeriesVarCommon(id);
    if (series_var_y1_indexes.length == 1) {
        $("#remove_series_var_y1_" + (series_var_y1_indexes[0])).button("disable");
    }
    updateSeriesSeriesBox();
}
function addSeriesVariableEns() {
    var last_index;

    if (series_var_y1_indexes.length > 0) {
        last_index = series_var_y1_indexes[series_var_y1_indexes.length - 1];
    } else {
        last_index = 0;
    }
    series_var_y1_indexes.push(last_index + 1);

    var series_var, remove_var, series_var_val, group_series_var, group_series_var_label;
    if (last_index == 0) {
        $('#series_var_table_y1').css("display", "");
        series_var = $("#series_var_y1_" + (last_index + 1));
        $("#remove_series_var_y1_" + (last_index + 1)).button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        }).click(function () {
                    removeSeriesVarSeriesBox($(this).attr('id'));
                });
        series_var_val = $("#series_var_val_y1_" + (last_index + 1));

    } else {
        series_var = $("#series_var_y1_" + last_index).clone(false);
        series_var.attr("id", 'series_var_y1_' + (last_index + 1));
        series_var.css("display", '');
    }

    if (last_index > 0) {
        remove_var = $("#remove_series_var_y1_" + last_index).button("enable").clone(true)
                .attr("id", 'remove_series_var_y1_' + (last_index + 1));

        series_var_val = $("#series_var_val_y1_" + last_index).clone(false);
        series_var_val.attr("id", 'series_var_val_y1_' + (last_index + 1));
        series_var_val.css("display", '');


        $('#series_var_table_y1').append($('<tr>').append($('<td>').append(remove_var)).append($('<td>').append(series_var)).append($('<td>').append(series_var_val)));
    }
    series_var_val.multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select value",
        click: function () {
            updateSeriesEns();
        },
        checkAll: function () {
            updateSeriesSeriesBox(true);
        },
        uncheckAll: function () {
            updateSeriesSeriesBox();
        }

    });
    try {
        series_var_val.multiselect("uncheckAll");
    } catch (err) {

    }
    series_var.multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        click: function () {
            var id_array = this.id.split("_");
            updateSeriesVarValEns(id_array[id_array.length - 1], []);
        }
    });
}


function addSeriesVariableRhist() {
    var last_index;

    if (series_var_y1_indexes.length > 0) {
        last_index = series_var_y1_indexes[series_var_y1_indexes.length - 1];
    } else {
        last_index = 0;
    }
    series_var_y1_indexes.push(last_index + 1);

    var series_var, remove_var, series_var_val, var_val_date_period_button, dialog;
    if (last_index == 0) {
        $('#series_var_table_y1').css("display", "");
        series_var = $("#series_var_y1_" + (last_index + 1));
        $("#remove_series_var_y1_" + (last_index + 1)).button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        }).click(function () {
                    removeSeriesVarCommon($(this).attr('id'));
                                        updateSeriesRhist();
                });
        series_var_val = $("#series_var_val_y1_" + (last_index + 1));
        var_val_date_period_button = $("#series_var_val_y1_date_period_button_" + (last_index + 1));
        dialog = $("#series_var_val_y1_date_period_" + (last_index + 1));


    } else {
        series_var = $("#series_var_y1_" + last_index).clone(false);
        series_var.attr("id", 'series_var_y1_' + (last_index + 1));
        series_var.css("display", '');
    }

    if (last_index > 0) {
        remove_var = $("#remove_series_var_y1_" + last_index).button("enable").clone(true)
                .attr("id", 'remove_series_var_y1_' + (last_index + 1));

        series_var_val = $("#series_var_val_y1_" + last_index).clone(false);
        series_var_val.attr("id", 'series_var_val_y1_' + (last_index + 1));
        series_var_val.css("display", '');

        var_val_date_period_button = $("<button>", {
            text: 'Select multiple options',
            id: 'series_var_val_y1_date_period_button_' + (last_index + 1)
        }).css('display', 'none');
        dialog = $("#series_var_val_y1_date_period_" + (last_index )).clone(false).appendTo( "body" );
        dialog.prop("id", "series_var_val_y1_date_period_" + (last_index + 1));
        dialog.find("#series_var_val_y1_date_period_start_" + (last_index )).prop('id', 'series_var_val_y1_date_period_start_' + (last_index + 1));
        dialog.find("#series_var_val_y1_date_period_end_" + (last_index )).prop('id', 'series_var_val_y1_date_period_end_' + (last_index + 1));
        dialog.find("#series_var_val_y1_date_period_by_" + (last_index )).prop('id', 'series_var_val_y1_date_period_by_' + (last_index + 1));
        dialog.find("#series_var_val_y1_date_period_by_unit_" + (last_index )).prop('id', 'series_var_val_y1_date_period_by_unit_' + (last_index + 1));
        $('body').append(dialog);


        $('#series_var_table_y1').append($('<tr>').append($('<td>').append(remove_var)).append($('<td>').append(series_var)).append($('<td>').append(series_var_val)).append($('<td>').append(var_val_date_period_button)));
    }
    createValDatePeriodDialog("series_var_val_y1", (last_index + 1));

    series_var_val.multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select value",
        click: function () {
            updateSeriesRhist();
        },
        checkAll: function () {
            updateSeriesRhist();
        },
        uncheckAll: function () {
            updateSeriesRhist();
        }

    });
    try {
        series_var_val.multiselect("uncheckAll");
    } catch (err) {

    }
    series_var.multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        click: function (event, ui) {
            $('#series_var_val_y1_date_period_start_' + (last_index + 1)).empty();
            $('#series_var_val_y1_date_period_end_' + (last_index + 1)).empty();

            if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
                $("#series_var_val_y1_date_period_button_" + (last_index + 1)).css("display", "block");
            } else {
                $("#series_var_val_y1_date_period_button_" + (last_index + 1)).css("display", "none");
            }
            var id_array = this.id.split("_");
            updateSeriesVarValRhist(id_array[id_array.length - 1], []);
        }
    });
    var_val_date_period_button.button({
        icons: {
            primary: "ui-icon-check",
            secondary: "ui-icon-circlesmall-plus"
        },
        text: false
    }).click(function () {
        $("#series_var_val_y1_date_period_" + (last_index + 1)).dialog("open");
    });
}


function addSeriesVariableSeriesBox(y_axis) {
    var last_index;
    if (y_axis == 'y1') {
        if (series_var_y1_indexes.length > 0) {
            last_index = series_var_y1_indexes[series_var_y1_indexes.length - 1];
        } else {
            last_index = 0;
        }
        series_var_y1_indexes.push(last_index + 1);
    } else {
        if (series_var_y2_indexes.length > 0) {
            last_index = series_var_y2_indexes[series_var_y2_indexes.length - 1];
        } else {
            last_index = 0;
        }
        series_var_y2_indexes.push(last_index + 1);
    }
    var series_var, remove_var, series_var_val, group_series_var, group_series_var_label,var_val_date_period_button, dialog;
    if (last_index == 0) {
        $('#series_var_table_' + y_axis).css("display", "");
        series_var = $("#series_var_" + y_axis + "_" + (last_index + 1));
        $("#remove_series_var_" + y_axis + "_" + (last_index + 1)).button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        }).click(function () {
                    removeSeriesVarSeriesBox($(this).attr('id'));
                });
        series_var_val = $("#series_var_val_" + y_axis + "_" + (last_index + 1));
        var_val_date_period_button = $("#series_var_val_"+y_axis+"_date_period_button_" + (last_index + 1));
        dialog = $("#series_var_val_"+y_axis+"_date_period_" + (last_index + 1));


    } else {
        series_var = $("#series_var_" + y_axis + "_" + last_index).clone(false);
        series_var.attr("id", 'series_var_' + y_axis + "_" + (last_index + 1));
        series_var.css("display", '');
    }

    if (last_index > 0) {
        remove_var = $("#remove_series_var_" + y_axis + "_" + last_index).button("enable").clone(true)
                .attr("id", 'remove_series_var_' + y_axis + "_" + (last_index + 1));

        series_var_val = $("#series_var_val_" + y_axis + "_" + last_index).clone(false);
        series_var_val.attr("id", 'series_var_val_' + y_axis + "_" + (last_index + 1));
        series_var_val.css("display", '');

        group_series_var = $("#group_series_var_" + y_axis + "_" + last_index).clone(false);
        group_series_var.attr("id", 'group_series_var_' + y_axis + "_" + (last_index + 1)).prop('checked', false);
        group_series_var.click(function () {
            updateSeriesSeriesBox();
        });
        group_series_var_label = $('<label for="' + 'group_series_var_' + y_axis + "_" + (last_index + 1) + '">Group_' + y_axis + '_' + (last_index + 1) + '</label>');
        var_val_date_period_button = $("<button>", {
            text: 'Select multiple options',
            id: 'series_var_val_' + y_axis + '_date_period_button_' + (last_index + 1)
        }).css('display', 'none');
        dialog = $("#series_var_val_" + y_axis + "_date_period_" + (last_index )).clone(false).appendTo( "body" );
        dialog.prop("id", "series_var_val_" + y_axis + "_date_period_" + (last_index + 1));
        dialog.find("#series_var_val_" + y_axis + "_date_period_start_" + (last_index )).prop('id', 'series_var_val_' + y_axis + '_date_period_start_' + (last_index + 1));
        dialog.find("#series_var_val_" + y_axis + "_date_period_end_" + (last_index )).prop('id', 'series_var_val_' + y_axis + '_date_period_end_' + (last_index + 1));
        dialog.find("#series_var_val_" + y_axis + "_date_period_by_" + (last_index )).prop('id', 'series_var_val_' + y_axis + '_date_period_by_' + (last_index + 1));
        dialog.find("#series_var_val_" + y_axis + "_date_period_by_unit_" + (last_index )).prop('id', 'series_var_val_' + y_axis + '_date_period_by_unit_' + (last_index + 1));
        $('body').append(dialog);

        $('#series_var_table_' + y_axis).append($('<tr>').append($('<td>').append(remove_var)).append($('<td>').append(series_var)).append($('<td>').append(series_var_val)).append($('<td>').append(var_val_date_period_button)).append($('<td>').append(group_series_var).append(group_series_var_label)));
    }
    createValDatePeriodDialog("series_var_val_"+y_axis, (last_index + 1) );

    series_var_val.multiselect({
        selectedList: 100, // 0-based index
        noneSelectedText: "Select value",
        click: function () {
            updateSeriesSeriesBox();
        },
        checkAll: function () {
            updateSeriesSeriesBox(true);
        },
        uncheckAll: function () {
            updateSeriesSeriesBox();
        }
    });
    try{
        series_var_val.multiselect("uncheckAll");
    }catch (err){

    }
    series_var.multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        click: function (event, ui) {
            $('#series_var_val_' + y_axis + '_date_period_start_' + (last_index + 1)).empty();
            $('#series_var_val_' + y_axis + '_date_period_end_' + (last_index + 1)).empty();

            if (ui.value == "fcst_init_beg" || ui.value == "fcst_valid_beg" || ui.value == "fcst_valid" || ui.value == "fcst_init") {
                $("#series_var_val_" + y_axis + "_date_period_button_" + (last_index + 1)).css("display", "block");
            } else {
                $("#series_var_val_" + y_axis + "_date_period_button_" + (last_index + 1)).css("display", "none");
            }
            var id_array = this.id.split("_");
            updateSeriesVarValSeries(id_array[id_array.length - 2], id_array[id_array.length - 1], []);
        }
    });
    var_val_date_period_button.button({
        icons: {
            primary: "ui-icon-check",
            secondary: "ui-icon-circlesmall-plus"
        },
        text: false
    }).click(function () {
        $("#series_var_val_" + y_axis + "_date_period_" + (last_index + 1)).dialog("open");
    });
}



function removeFcstVarSeriesBox(id) {
    var id_array = id.split("_");
    var index = id_array[id_array.length - 1];
    var y_axis = id_array[id_array.length - 2];
    var index_of_removing_el;
    //destroy selects
    $("#fcst_var_" + y_axis + "_" + index).multiselect("destroy");
    $("#fcst_stat_" + y_axis + "_" + index).multiselect("destroy");

    if (y_axis == 'y1') {
        index_of_removing_el = jQuery.inArray(parseInt(index), fcst_var_y1_indexes);
        if (index_of_removing_el > -1) {
            fcst_var_y1_indexes.splice(index_of_removing_el, 1);
        }
        if (fcst_var_y1_indexes.length == 1) {
            $("#remove_fcst_var_" + y_axis + "_" + (fcst_var_y1_indexes[0])).button("disable");
        }
        if (fcst_var_y1_indexes.length > 0) {
            $("#fcst_var_" + y_axis + "_" + index).parent().parent().remove();
        } else {
            $('#dependent_var_table_' + y_axis).css("display", "none");
        }
    } else {
        index_of_removing_el = jQuery.inArray(parseInt(index), fcst_var_y2_indexes);
        if (index_of_removing_el > -1) {
            fcst_var_y2_indexes.splice(index_of_removing_el, 1);
        }

        if (fcst_var_y2_indexes.length > 0) {
            $("#fcst_var_" + y_axis + "_" + index).parent().parent().remove();
        } else {
            $('#dependent_var_table_' + y_axis).css("display", "none");
        }
    }
    updateSeriesSeriesBox();
}

function addFcstVariableSeries(y_axis) {
    var last_index;
    var selected_mode = $("#plot_data").multiselect("getChecked").val();
    if (y_axis == 'y1') {
        if (fcst_var_y1_indexes.length > 0) {
            last_index = fcst_var_y1_indexes[fcst_var_y1_indexes.length - 1];
        } else {
            last_index = 0;
        }
        fcst_var_y1_indexes.push(last_index + 1);
    } else {
        if (fcst_var_y2_indexes.length > 0) {
            last_index = fcst_var_y2_indexes[fcst_var_y2_indexes.length - 1];
        } else {
            last_index = 0;
        }
        fcst_var_y2_indexes.push(last_index + 1);
    }

    var fcst_var, remove_var, fcst_stat, fcst_stat_mode, fcst_stat_mode_config;
    if (last_index == 0) {
        $('#dependent_var_table_' + y_axis).css("display", "");
        fcst_var = $("#fcst_var_" + y_axis + "_" + (last_index + 1));
        $("#remove_fcst_var_" + y_axis + "_" + (last_index + 1)).button({
            icons: {
                primary: "ui-icon-trash"
            },
            text: false
        }).click(function () {
                    removeFcstVarSeriesBox($(this).attr('id'));
                });
        fcst_stat = $("#fcst_stat_" + y_axis + "_" + (last_index + 1));
        fcst_stat_mode = $("#fcst_stat_mode_" + y_axis + "_" + last_index);


    } else {
        fcst_var = $("#fcst_var_" + y_axis + "_" + last_index).clone(false);
        fcst_var.attr("id", 'fcst_var_' + y_axis + "_" + (last_index + 1));
        fcst_var.css("display", '');
    }

    if (last_index > 0) {
        remove_var = $("#remove_fcst_var_" + y_axis + "_" + last_index).button("enable").clone(true)
                .attr("id", 'remove_fcst_var_' + y_axis + "_" + (last_index + 1));

        fcst_stat = $("#fcst_stat_" + y_axis + "_" + last_index).clone(false);
        fcst_stat.attr("id", 'fcst_stat_' + y_axis + "_" + (last_index + 1));
        fcst_stat.css("display", '');

        fcst_stat_mode = $("#fcst_stat_mode_" + y_axis + "_" + last_index).clone(false);
        fcst_stat_mode.attr("id", 'fcst_stat_mode_' + y_axis + "_" + (last_index + 1));
        fcst_stat_mode_config = $("#fcst_stat_mode_config_" + y_axis + "_" + last_index).clone(true);
        fcst_stat_mode_config.attr("id", 'fcst_stat_mode_config_' + y_axis + "_" + (last_index + 1));
        fcst_stat_mode_config.css("display", 'none');

        $('#dependent_var_table_' + y_axis).append($('<tr>').append($('<td>').append(remove_var)).append($('<td>').append(fcst_var)).append($('<td>').append(fcst_stat).append('<br/>').append(fcst_stat_mode)).append($('<td>').append(fcst_stat_mode_config)));
    }
    fcst_var.multiselect({
        multiple: false,
        selectedList: 1,
        header: false,
        minWidth: 'auto',
        height:200,
        click: function () {
            var id_array = this.id.split("_");
            updateStats(id_array[id_array.length - 2], id_array[id_array.length - 1], []);
            var selectedSeriesVarVal;
            try{
                selectedSeriesVarVal = $("#series_var_val_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("getChecked").val();
               }catch (err){
                selectedSeriesVarVal = $("#series_var_val_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]+ ' option:first-child').val();
               }

            if(selectedSeriesVarVal == null){
                selectedSeriesVarVal = [];
            }
            updateSeriesVarValSeries(id_array[id_array.length - 2], id_array[id_array.length - 1],selectedSeriesVarVal);
        }
    });
    if (selected_mode == "stat") {
        fcst_stat.multiselect({
            selectedList: 100, // 0-based index
            noneSelectedText: "Select attribute stat",
            click: function (event, ui) {
                updateSeriesSeriesBox();
            },
            position: {
                my: 'right center',
                at: 'right center'
            },
            checkAll: function () {

                updateSeriesSeriesBox();
            },
            uncheckAll: function () {
                updateSeriesSeriesBox();
            }
        });

    } else {

        fcst_stat.multiselect({
            selectedList: 100, // 0-based index
            noneSelectedText: "Select ratio stat",
            position: {
                my: 'right center',
                at: 'right center'
            },
            click: function (event, ui) {
                var id_array = this.id.split("_");
                $("#fcst_stat_mode_config_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).css("display", "none");
                try {
                    $("#fcst_stat_mode_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("uncheckAll");
                } catch (err) {
                    console.log("Error " + err);
                }
                updateSeriesSeriesBox();
            },
            checkAll: function () {
                updateSeriesSeriesBox();
            },
            uncheckAll: function () {
                updateSeriesSeriesBox();
            }
        });
        if ($("#fcst_var_" + y_axis + "_" + (last_index + 1)).multiselect("getChecked").val() == "N/A") {
            fcst_stat.multiselect("disable");
        } else {
            fcst_stat.multiselect("enable");
        }
        fcst_stat_mode.multiselect({
            multiple: false,
            selectedList: 1,
            header: false,
            minWidth: 225,
            height: 300,
            noneSelectedText: "Select attribute stat",
            allUnselected: true,
            position: {
                my: 'right center',
                at: 'right center'

            },
            click: function (event, ui) {
                var id_array = this.id.split("_");
                $("#fcst_stat_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]).multiselect("uncheckAll");
                var config_table = $("#fcst_stat_mode_config_" + id_array[id_array.length - 2] + "_" + id_array[id_array.length - 1]);
                config_table.css("display", "block");
                if (ui.value == "ACOV") {
                    config_table.find(".non-acov").attr("disabled", true);
                } else {
                    config_table.find(".non-acov").removeAttr("disabled");
                }
                if (listStatModePair.indexOf(ui.value) > -1) {
                    config_table.find(".non-pair").attr("disabled", true);
                } else {
                    config_table.find(".non-pair").removeAttr("disabled");
                }

                updateSeriesSeriesBox();
            }
        });
    }
}
function loadXMLRoc() {
    var series_var_val;
    if (initXML.find("plot").find("series1").children().length > 0) {
        var series_arr = initXML.find("plot").find("series1").children();
        for (var i = 0; i < series_arr.length; i++) {
            series_var_val = [];
            addSeriesVariableRhist();

            var value = $(series_arr[i]).attr('name');
            $("#series_var_y1_" + (i + 1)).val(value).multiselect("refresh");
            $(series_arr[i]).find("val").each(function () {
                series_var_val.push($(this).text());
            });
            if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                $("#series_var_val_y1_date_period_button_" + (i + 1)).css("display", "block");
            } else {
                $("#series_var_val_y1_date_period_button_" + (i + 1)).css("display", "none");
            }
            updateSeriesVarValRhist((i + 1), series_var_val);

        }
    } else {
        series_var_val = $("#series_var_y1_1").first().val();
        updateSeriesVarValRhist(1, series_var_val);
    }
    updatePlotFix();
    var roc_pct = $(initXML.find("plot").find("roc_calc").find("roc_pct")).text();
    var roc_ctc = $(initXML.find("plot").find("roc_calc").find("roc_ctc")).text();
    $("input[name=roc_type][value=pct]").prop('checked', roc_pct == "TRUE");
    $("input[name=roc_type][value=ctc]").prop('checked', roc_ctc == "TRUE");

}

function loadXMLRhist() {
    var series_var_val;
    if (initXML.find("plot").find("series1").children().length > 0) {
        var series_arr = initXML.find("plot").find("series1").children();

        for (var i = 0; i < series_arr.length; i++) {
            series_var_val = [];
            addSeriesVariableRhist();
            var value = $(series_arr[i]).attr('name');
            $("#series_var_y1_" + (i + 1)).val(value).multiselect("refresh");
            $(series_arr[i]).find("val").each(function () {
                var vals = $(this).text().split(",");

                for (var k = 0; k < vals.length; k++) {
                    series_var_val.push(vals[k]);
                }
            });
            if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                $("#series_var_val_y1_date_period_button_" + (i + 1)).css("display", "block");
            } else {
                $("#series_var_val_y1_date_period_button_" + (i + 1)).css("display", "none");
            }
            updateSeriesVarValRhist((i + 1), series_var_val);

        }
    } else {
        series_var_val = $("#series_var_1"  + "_1").first().val();
        updateSeriesVarValRhist( 1, series_var_val);
    }
    updatePlotFix();
    $("input[name=normalized_histogram][value=" + $(initXML.find("plot").find("normalized_histogram")).text() + "]").prop('checked', true);
}

function loadXMLRely() {
    var series_var_val;
       if (initXML.find("plot").find("series1").children().length > 0) {
           var series_arr = initXML.find("plot").find("series1").children();
           for (var i = 0; i < series_arr.length; i++) {
               series_var_val = [];
               addSeriesVariableRhist();

               var value=$(series_arr[i]).attr('name');
               $("#series_var_y1_" + (i + 1)).val(value).multiselect("refresh");
               $(series_arr[i]).find("val").each(function () {
                   series_var_val.push($(this).text());
               });
               if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                   $("#series_var_val_y1_date_period_button_"+ (i + 1)).css("display", "block");
               } else {
                   $("#series_var_val_y1_date_period_button_"+ (i + 1)).css("display", "none");
               }
               updateSeriesVarValRhist((i + 1), series_var_val);

           }
       } else {
           series_var_val = $("#series_var_y1_1").first().val();
           updateSeriesVarValRhist(1, series_var_val);
       }

    updatePlotFix();
    var rely_event_hist = $(initXML.find("plot").find("rely_event_hist")).text();

    $("input[name=rely_event_hist][value=true]").prop('checked', rely_event_hist == "TRUE" || rely_event_hist == "true");
    $("input[name=rely_event_hist][value=false]").prop('checked', rely_event_hist == "FALSE" || rely_event_hist == "false");

}

function loadXMLEns() {
    var series_var_val;
    if (initXML.find("plot").find("series1").children().length > 0) {
        var series_arr = initXML.find("plot").find("series1").children();
        for (var i = 0; i < series_arr.length; i++) {
            series_var_val = [];
            if (i != 0) {
                addSeriesVariableEns();
            }
            var value=$(series_arr[i]).attr('name');
            $("#series_var_y1_" + (i + 1)).val(value).multiselect("refresh");
            $(series_arr[i]).find("val").each(function () {
                series_var_val.push($(this).text());
            });
            if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                $("#series_var_val_y1_date_period_button_"+ (i + 1)).css("display", "block");
            } else {
                $("#series_var_val_y1_date_period_button_"+ (i + 1)).css("display", "none");
            }
            updateSeriesVarValEns((i + 1), series_var_val);

        }
    } else {
        series_var_val = $("#series_var_y1_1").first().val();
        updateSeriesVarValEns(1, series_var_val);
    }

    updatePlotFix();
    var ensss_pts = $(initXML.find("plot").find("ensss_pts")).text();
    $("#ensss_pts").val(ensss_pts);

    var ensss_pts_disp = $(initXML.find("plot").find("ensss_pts_disp")).text();
    $("input[name=ensss_pts_disp][value=true]").prop('checked', ensss_pts_disp == "TRUE" || ensss_pts_disp == "true");
    $("input[name=ensss_pts_disp][value=false]").prop('checked', ensss_pts_disp == "FALSE" || ensss_pts_disp == "false");

    updateSeriesEns();

}

function updatePlotFix() {
    if ($(initXML.find("plot").find("plot_fix")).children().length > 0) {
        var plot_fix_arr = $(initXML.find("plot").find("plot_fix")).children();
        for (var i = 0; i < plot_fix_arr.length; i++) {
            var fixed_var_vals = [];
            if (i > 0 ) {
                addFixedVariableRhist();
            }
            var value = $(plot_fix_arr[i]).attr('name');
            $("#fixed_var_" + (i + 1)).val(value).multiselect("refresh");

            $(plot_fix_arr[i]).find("set").find("val").each(function () {
                fixed_var_vals.push($(this).text());
            });
            if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                $("#fixed_var_val_date_period_button_" + (i + 1)).css("display", "block");
            } else {
                $("#fixed_var_val_date_period_button_" + (i + 1)).css("display", "none");
            }
            updateFixedVarValHist((i + 1), fixed_var_vals);
        }

    }
}

function updatePlotFixSeries() {
    if ($(initXML.find("plot").find("plot_fix")).children().length > 0) {
        var plot_fix_arr = $(initXML.find("plot").find("plot_fix")).children();
        for (var i = 0; i < plot_fix_arr.length; i++) {
            var fixed_var_vals = [];

            addFixedVariableSeries();
            var value = $(plot_fix_arr[i]).attr('name');
            $("#fixed_var_" + (i + 1)).val(value).multiselect("refresh");

            $(plot_fix_arr[i]).find("set").find("val").each(function () {
                fixed_var_vals.push($(this).text());
            });
            if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                $("#fixed_var_val_date_period_button_" + (i + 1)).css("display", "block");
            } else {
                $("#fixed_var_val_date_period_button_" + (i + 1)).css("display", "none");
            }
            updateFixedVarValSeries((i + 1), fixed_var_vals);
        }

    }
}


function loadXMLSeries() {
    //Parse the plot_type from _strInitXML using assumptions about the first stat
    var stat = $(initXML.find("plot").find("dep").find("dep1").find("stat")[0]).text();
    var selected_mode = isModeStat(stat) ? "mode" : "stat";
    $("#plot_data").val(selected_mode).multiselect("refresh");
    updateForecastVariables();
    $("#plot_stat").val(initXML.find("plot").find("plot_stat").text()).multiselect("refresh");
    if (selected_mode == "stat") {
        $.each(fix_var_value_to_title_stat_map, function (key, val) {
            $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
        });
    } else {
        $.each(fix_var_value_to_title_mode_map, function (key, val) {
            $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
        });
    }
    var value;
    for (var y_axis_index = 1; y_axis_index <= 2; y_axis_index++) {
        var y_axis = "y" + y_axis_index;
        var index=0;

        if (initXML.find("plot").find("dep").find("dep" + y_axis_index).children().length > 0) {
            var dep_arr = initXML.find("plot").find("dep").find("dep" + y_axis_index).children();
            for (var i = 0; i < dep_arr.length; i++) {
                var fcst_stat = [];
                value = $(dep_arr[i]).attr('name');
                if (index == 0 && y_axis_index == 1) {
                    $("#fcst_var_" + y_axis + "_" + (index + 1)).val(value).multiselect("refresh");
                } else {
                    addFcstVariableSeries(y_axis);
                    $("#fcst_var_" + y_axis + "_" + (index + 1)).val(value).multiselect("refresh");
                }
                $(dep_arr[i]).find("stat").each(function () {
                    fcst_stat.push($(this).text());
                });


                if (selected_mode == 'stat') {
                    updateStats(y_axis, index+1, fcst_stat);
                    index++;
                } else {
                    if($.isArray(fcst_stat)){
                        for(var fcst_stat_ind = 0; fcst_stat_ind < fcst_stat.length; fcst_stat_ind++){
                            if(fcst_stat_ind > 0){
                                addFcstVariableSeries(y_axis);
                                $("#fcst_var_" + y_axis + "_" + (index + 1)).val($(dep_arr[i]).attr('name')).multiselect("refresh");
                            }
                            updateMode(y_axis, index + 1, fcst_stat[fcst_stat_ind]);
                            index++;
                        }
                    }else{
                        updateMode(y_axis, index + 1, fcst_stat);
                        index++;
                    }
                }
            }
        } else {
            if (selected_mode == 'stat') {
                updateStats(y_axis, 1, "");
            } else {
                updateMode(y_axis, 1, "");
            }
        }
        var series_var_val, isGroup;
        if (initXML.find("plot").find("series" + y_axis_index).children().length > 0) {
            var series_arr = initXML.find("plot").find("series" + y_axis_index).children();

            for (var i = 0; i < series_arr.length; i++) {
                series_var_val = [];
                isGroup = false;
                value =  $(series_arr[i]).attr('name');
                if (i == 0 && y_axis_index == 1) {
                    $("#series_var_" + y_axis + "_" + ( i + 1)).val(value).multiselect("refresh");
                } else {
                    addSeriesVariableSeriesBox(y_axis);
                    $("#series_var_" + y_axis + "_" + (i + 1)).val(value).multiselect("refresh");
                }
                $(series_arr[i]).find("val").each(function () {
                    var vals = $(this).text().split(",");
                    if (vals.length > 1) {
                        isGroup = true;
                    }
                    for (var k = 0; k < vals.length; k++) {
                        series_var_val.push(vals[k]);
                    }
                });
                if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
                    $("#series_var_val_" + y_axis + "_date_period_button_" + (i + 1)).css("display", "block");
                } else {
                    $("#series_var_val_" + y_axis + "_date_period_button_" + (i + 1)).css("display", "none");
                }
                updateSeriesVarValSeries(y_axis, (i + 1), series_var_val);
                if (isGroup) {
                    $('#group_series_var_' + y_axis + "_" + (i + 1)).prop('checked', true);
                }
            }
        } else {
            series_var_val = $("#series_var_" + y_axis + "_1").first().val();
            updateSeriesVarValSeries(y_axis, 1, series_var_val);
        }
    }

    updatePlotFixSeries();
    $("#txtPlotCond").val(initXML.find("plot").find("plot_cond").text());
    //update indy var for mode
    if (selected_mode == 'mode') {
        updateIndyVarSeries(selected_mode);
    }

    value = $(initXML.find("plot").find("indep")[0]).attr('name');
    $("#indy_var").val(value).multiselect("refresh");
    if (value == "fcst_init_beg" || value == "fcst_valid_beg" || value == "fcst_valid" || value == "fcst_init") {
        $("#date_period_button").css("display", "block");
    } else {
        $("#date_period_button").css("display", "none");
    }
    var indy_var_vals = [];
    indy_var_vals_to_attr={};
    $(initXML.find("plot").find("indep")[0]).find("val").each(function () {
        indy_var_vals.push($(this).text());
        var obj = {};
        obj.label = $(this)[0].attributes.label.nodeValue;
        obj.plot_val = $(this)[0].attributes.plot_val.nodeValue;
        indy_var_vals_to_attr[$(this).text()]=obj;
    });
    if (indy_var_vals.length > 0) {
        $("#indy_var_val").multiselect("option", "indy_var_vals_to_attr", indy_var_vals_to_attr);
        populateIndyVarVal(indy_var_vals);
    }

    if (initXML.find("plot").find("agg_stat").length > 0) {
        $("input[name=statistics][value=aggregation_statistics]").prop('checked', true);
        $('#radio').buttonset('refresh');
        $('#statistics p').hide();
        $('#aggregation_statistics').show();

        $("input[name=agg_stat][value=ctc]").prop('checked', $(initXML.find("plot").find("agg_stat").find("agg_ctc")).text() == "TRUE");
        $("input[name=agg_stat][value=sl1l2]").prop('checked', $(initXML.find("plot").find("agg_stat").find("agg_sl1l2")).text() == "TRUE");
        $("input[name=agg_stat][value=pct]").prop('checked', $(initXML.find("plot").find("agg_stat").find("agg_pct")).text() == "TRUE");
        $("input[name=agg_stat][value=nbrcnt]").prop('checked', $(initXML.find("plot").find("agg_stat").find("agg_nbrcnt")).text() == "TRUE");


        $("#boot_repl").val($(initXML.find("plot").find("agg_stat").find("boot_repl")).text());
        $("#boot_ci").val($(initXML.find("plot").find("agg_stat").find("boot_ci")).text());
        $('#selEveqDis').prop('checked', $(initXML.find("plot").find("agg_stat").find("eveq_dis")).text() == "TRUE");
        $('#cacheAggStat').prop('checked', $(initXML.find("plot").find("agg_stat").find("cache_agg_stat")).text() == "TRUE");

    } else if (initXML.find("plot").find("calc_stat").length > 0) {
        $("input[name=statistics][value=calculations_statistics]").prop('checked', true);
        $('#radio').buttonset('refresh');
        $('#statistics p').hide();
        $('#calculations_statistics').show();
        $("input[name=calc_stat][value=ctc]").prop('checked', $(initXML.find("plot").find("calc_stat").find("calc_ctc")).text() == "TRUE");
        $("input[name=calc_stat][value=sl1l2]").prop('checked', $(initXML.find("plot").find("calc_stat").find("calc_sl1l2")).text() == "TRUE");
    } else {
        $("input[name=statistics][value=none]").prop('checked', true);
        $('#radio').buttonset('refresh');
        $('#statistics p').hide();
        $('#none').show();
    }


    var listdiffseries1 = $(initXML.find("plot").find("tmpl").find("listDiffSeries1")).text();
    if (listdiffseries1.length == 0) {
        listdiffseries1 = "list()";
    }

    var listdiffseries2 = $(initXML.find("plot").find("tmpl").find("listDiffSeries2")).text();
    if (listdiffseries2.length == 0) {
        listdiffseries2 = "list()";
    }
    if (listdiffseries1 != "list()") {
        var t1 = listdiffseries1.split("c(");
        for (var i = 1; i < t1.length; i++) {
            t1[i] = t1[i].replace(/\(/g, "").replace(/\)/g, "").replace(/"/g, "");
            seriesDiffY1.push(t1[i]);
        }
    }
    if (listdiffseries2 != "list()") {
        var t2 = listdiffseries2.split("c(");
        for (var i = 1; i < t2.length; i++) {
            t2[i] = t2[i].replace(")", "").replace(")", "").replace("\"", "");
            seriesDiffY2.push(t2[i]);
        }
    }

    updateSeriesSeriesBox();


}

function requestDBUpdate() {

    $.ajax({
        async: false,
        url: "servlet",
        type: "POST",
        dataType: 'xml',
        data: '<request><list_db_update></list_db_update></request>',
        error: function (jqXHR, textStatus, errorThrown) {

        },
        success: function (data) {
            var values = $(data).find("val");
            var databaseEl = $("#database");
            databaseEl.empty();
            var selected, selectedDatabase;
            if (initXML != null) {
                selectedDatabase = initXML.find("database").text();
            }
            for (var i = 0; i < values.length; i++) {
                var t = $(values[i]);
                if (selectedDatabase != null) {
                    selected = t.text() == selectedDatabase;
                } else {
                    selected = i == 0;
                }

                var opt = $('<option />', {
                    value: t.text(),
                    text: t.text(),
                    selected: selected
                });
                opt.appendTo(databaseEl);
            }
            databaseEl.multiselect('refresh');
            $('#listdt').jqGrid('GridUnload');
            initPage();
        }});

}
function updateFixVarSeries(selected_mode) {
    //remove fixed values
    var fixed_var_indexes_copy = fixed_var_indexes.slice();
    for (var i = 0; i < fixed_var_indexes_copy.length; i++) {
        removeFixedVarSeries("fixed_var_" + fixed_var_indexes_copy[i]);
    }
    $('#fixed_var_1').empty();
    if (selected_mode == "stat") {
        $.each(fix_var_value_to_title_stat_map, function (key, val) {
            $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
        });
    } else {
        $.each(fix_var_value_to_title_mode_map, function (key, val) {
            $('#fixed_var_1').append('<option value="' + key + '">' + val + '</option>');
        });
    }
    try{
        $('#fixed_var_1').multiselect('refresh');
    }catch (err){}

}

function updateIndyVarSeries(selected_mode) {

    $('#indy_var').empty();
    if (selected_mode == "stat") {
        $.each(indy_var_value_to_title_stat_map, function (key, val) {
            $('#indy_var').append('<option value="' + key + '">' + val + '</option>');
        });
    } else {
        $.each(indy_var_value_to_title_mode_map, function (key, val) {
            $('#indy_var').append('<option value="' + key + '">' + val + '</option>');
        });
    }
    $('#indy_var').multiselect('refresh');
    $("#date_period").css("display", "none");

}

String.prototype.endsWith = function (suffix) {
    return this.indexOf(suffix, this.length - suffix.length) !== -1;
};

function createValDatePeriodDialog(prefix, var_index){
    var start_el = $('#' + prefix +'_date_period_start_' + (var_index));
    var end_el = $('#' + prefix +'_date_period_end_' + (var_index));

    $("#" + prefix +"_date_period_" + (var_index )).dialog({
        width: 295,
        autoOpen: false,
        position: {
            my: "left bottom",
            at: 'left top',
            of: "#" + prefix +"_date_period_button_" + (var_index)
        },
        resizable: false,
        draggable: false,
        buttons: {
            "Select values": function () {
                if ( start_el.val() && end_el.val()) {
                    var start = moment(start_el.val(), 'YYYY-MM-DD HH:mm:ss');//2007-08-15 12:00:00
                    var end = moment(end_el.val(), 'YYYY-MM-DD HH:mm:ss');
                    var by = $("#" + prefix +"_date_period_by_" + (var_index)).val().trim();
                    var unit = $("#" + prefix +"_date_period_by_unit_" +(var_index) ).val();

                    if (by.length == 0) {
                        by = 3600;
                    }
                    by = parseFloat(by);
                    if(unit == "min"){
                        by = by * 60;
                    }else if (unit == "hours"){
                        by = by * 3600;
                    } else if (unit == "days") {
                        by = by * 86400;
                    }
                    var dates;
                    if (prefix == 'fixed_var_val') {
                        dates = $(fixVarValResponse[var_index]).find("val");
                    } else if (prefix == 'series_var_val_y1') {
                        dates = $(seriesY1VarValResponse[var_index]).find("val");
                    } else if (prefix == 'series_var_val_y2') {
                        dates = $(seriesY2VarValResponse[var_index]).find("val");
                    }
                    var current_date = start.clone();
                    while (current_date <= end) {
                        var current_date_str = current_date.format('YYYY-MM-DD HH:mm:ss');
                        var isFound = false;
                        for (var i = 0; i < dates.length; i++) {
                            if ($(dates[i]).text() == current_date_str) {
                                isFound = true;
                                break;
                            }
                        }
                        if (isFound) {
                            $("#"+prefix +"_" + (var_index)).find('option[value="' + current_date_str + '"]').prop('selected', true);
                        }
                        current_date.add(by, 'seconds');
                    }
                    try{
                        $("#"+prefix +"_" + (var_index)).multiselect("refresh");
                    } catch (err) {
                        console.log(err)
                    }
                    if (prefix != 'fixed_var_val') {
                        if (currentTab == "Series" || currentTab == "Box" || currentTab == "Bar") {
                            updateSeriesSeriesBox();
                        } else if (currentTab == "Rhist" || currentTab == "Phist" || currentTab == "Roc" || currentTab == "Rely") {
                            updateSeriesRhist();
                        } else if (currentTab = "Ens_ss") {
                            updateSeriesEns();
                        }

                    }
                }

                $(this).dialog("close");
            }
        },
        open: function (event, ui) {
            var dates;
            if(prefix == 'fixed_var_val'){
                dates = $(fixVarValResponse[var_index]).find("val");
            }else if( prefix == 'series_var_val_y1'){
                dates = $(seriesY1VarValResponse[var_index]).find("val");
            }else if(prefix == 'series_var_val_y2'){
                dates = $(seriesY2VarValResponse[var_index]).find("val");
            }
            start_el.empty();
            end_el.empty();
            for (var i = 0; i < dates.length; i++) {
                var t = $(dates[i]);
                var option = $("<option/>")
                        .attr("value", t.text())
                        .text(t.text());
                if (t.text() == start_el.val()) {
                    option.prop("selected", true);
                }
                start_el.append(option);
                if (t.text() == end_el.val()) {
                    option.prop("selected", true);
                } else {
                    option.prop("selected", false);
                }
                end_el.append(option.clone());
            }
            end_el.find('option').last().prop('selected',true);
            if(dates.length > 1) {
                var start = moment($(dates[0]).text(), 'YYYY-MM-DD HH:mm:ss');
                var end = moment($(dates[1]).text(), 'YYYY-MM-DD HH:mm:ss');
                var d = moment.duration({from: start, to: end});
                $("#" + prefix +"_date_period_by_" + (var_index)).val(d.as('hours'));
            }
        }
    });
    
    
}

function createDatePeriodDialog(){
    var start_el = $('#date_period_start');
    var end_el = $('#date_period_end');
    var indy_var_val = $('#indy_var_val');
    $("#date_period_dialog").dialog({
                    width: 295,
                    autoOpen: false,
                    position: {
                        my: "left bottom",
                        at: 'left top',
                        of: "#date_period_button"
                    },
                    resizable: false,
                    draggable: false,
                    buttons: {
                        "Select values": function () {
                            if (start_el.val() && end_el.val()) {
                                var start = moment(start_el.val(), 'YYYY-MM-DD HH:mm:ss');//2007-08-15 12:00:00
                                var end = moment(end_el.val(), 'YYYY-MM-DD HH:mm:ss');
                                var by = $("#date_period_by").val().trim();
                                var unit = $("#date_period_by_unit").val();

                                if (by.length == 0) {
                                    by = 3600;
                                }
                                by = parseInt(by);
                                if(unit == "min"){
                                    by = by * 60;
                                }else if (unit == "hours"){
                                    by = by * 3600;
                                }else if (unit == "days") {
                                    by = by * 86400;
                                }
                                var dates = $(previousIndVarValResponse).find("val");
                                var current_date = start.clone();
                                while (current_date <= end) {
                                    var current_date_str = current_date.format('YYYY-MM-DD HH:mm:ss');
                                    var isFound = false;
                                    for (var i = 0; i < dates.length; i++) {
                                        if ($(dates[i]).text() == current_date_str) {
                                            isFound = true;
                                            break;
                                        }
                                    }
                                    if (isFound) {
                                        indy_var_val.find('option[value="' + current_date_str + '"]').prop('selected', true);
                                    }
                                    current_date.add(by, 'seconds');
                                }
                                indy_var_val.multiselect("refresh");
                            }

                            $(this).dialog("close");
                        }
                    },
                    open: function (event, ui) {
                        var values =indy_var_val.val();
                        if (values == null) {
                            values = [];
                        }
                        populateIndyVarVal(values);

                        var dates = $(previousIndVarValResponse).find("val");
                        var start_val= start_el.val();
                        var end_val= end_el.val();

                        start_el.empty();
                        end_el.empty();
                        for (var i = 0; i < dates.length; i++) {
                            var t = $(dates[i]);
                            var option = $("<option/>")
                                    .attr("value", t.text())
                                    .text(t.text());
                            if (t.text() == start_val) {
                                option.prop("selected", true);
                            }
                            start_el.append(option);
                            if (t.text() == end_val) {
                                option.prop("selected", true);
                            } else {
                                option.prop("selected", false);
                            }

                            end_el.append(option.clone());

                        }
                        end_el.find('option').last().prop('selected',true);
                        if(dates.length > 1) {
                            var start = moment($(dates[0]).text(), 'YYYY-MM-DD HH:mm:ss');
                            var end = moment($(dates[1]).text(), 'YYYY-MM-DD HH:mm:ss');
                            var d = moment.duration({from: start, to: end});
                            $("#date_period_by").val(d.as('hours'));
                        }
                    }
                });
}

