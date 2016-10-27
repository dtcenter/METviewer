-- data_file_type_lu is a look-up table containing information about the different types
--   of MET output data files.  Each data file that is loaded into the DATABASE is
--   represented by a record in the data_file table, which points at one of the data file
--   types.  The file type indicates which DATABASE tables store the data in the file.

DROP TABLE IF EXISTS data_file_lu;
CREATE TABLE data_file_lu
(
    data_file_lu_id     INT UNSIGNED NOT NULL,
    type_name           VARCHAR(32),
    type_desc           VARCHAR(128),
    PRIMARY KEY (data_file_lu_id)
) ENGINE = MyISAM;
    
    
-- data_file_id stores information about files that have been parsed and loaded into the
--   DATABASE.  Each record represents a single file of a particular MET output data file
--   type (point_stat, mode, etc.).  Each data_file record points at its file type in the
--   data_file_type_lu table via the data_file_type_lu_id field.

DROP TABLE IF EXISTS data_file;
CREATE TABLE data_file
(
    data_file_id        INT UNSIGNED NOT NULL,
    data_file_lu_id     INT UNSIGNED NOT NULL,
    filename            VARCHAR(256),
    path                VARCHAR(512),
    load_date           DATETIME,
    mod_date            DATETIME,
    PRIMARY KEY (data_file_id),
    CONSTRAINT data_file_unique_pk
        UNIQUE INDEX (filename, path),
    CONSTRAINT stat_header_data_file_lu_id_pk
            FOREIGN KEY(data_file_lu_id)
            REFERENCES data_file_lu(data_file_lu_id)
) ENGINE = MyISAM;



-- stat_header contains the forecast and observation bookkeeping information, except for
--   the valid and init times, for a verification case.  Statistics tables point at a
--   single stat_header record, which indicate the circumstances under which they were
--   calculated.

DROP TABLE IF EXISTS stat_header;
CREATE TABLE stat_header
(
    stat_header_id      INT UNSIGNED NOT NULL,
    version             VARCHAR(8),
    model               VARCHAR(64),
    fcst_var            VARCHAR(64),
    fcst_lev            VARCHAR(16),
    obs_var             VARCHAR(64),
    obs_lev             VARCHAR(16),
    obtype              VARCHAR(32),
    vx_mask             VARCHAR(32),
    interp_mthd         VARCHAR(16),
    interp_pnts         INT UNSIGNED,
    fcst_thresh         VARCHAR(128),
    obs_thresh          VARCHAR(128),
    
    PRIMARY KEY          (stat_header_id),
    
    CONSTRAINT stat_header_unique_pk
        UNIQUE INDEX (
            model,
            fcst_var,
            fcst_lev,
            obtype,
            vx_mask,
            interp_mthd,
            interp_pnts,
            fcst_thresh,
            obs_thresh
        )
) ENGINE = MyISAM;


-- line_data_fho contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_fho;
CREATE TABLE line_data_fho
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    f_rate              DOUBLE,
    h_rate              DOUBLE,
    o_rate              DOUBLE,
    
    CONSTRAINT line_data_fho_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_fho_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_ctc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_ctc;
CREATE TABLE line_data_ctc
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    fy_oy               INT UNSIGNED,   
    fy_on               INT UNSIGNED,   
    fn_oy               INT UNSIGNED,   
    fn_on               INT UNSIGNED,   
       
    CONSTRAINT line_data_ctc_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_ctc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
    ,
        INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_cts contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_cts;
CREATE TABLE line_data_cts
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    alpha               DOUBLE,
    total               INT UNSIGNED,

    baser               DOUBLE,
    baser_ncl           DOUBLE,
    baser_ncu           DOUBLE,
    baser_bcl           DOUBLE,
    baser_bcu           DOUBLE,
    fmean               DOUBLE,
    fmean_ncl           DOUBLE,
    fmean_ncu           DOUBLE,
    fmean_bcl           DOUBLE,
    fmean_bcu           DOUBLE,
    acc                 DOUBLE,
    acc_ncl             DOUBLE,
    acc_ncu             DOUBLE,
    acc_bcl             DOUBLE,
    acc_bcu             DOUBLE,
    fbias               DOUBLE,
    fbias_bcl           DOUBLE,
    fbias_bcu           DOUBLE,
    pody                DOUBLE,
    pody_ncl            DOUBLE,
    pody_ncu            DOUBLE,
    pody_bcl            DOUBLE,
    pody_bcu            DOUBLE,
    podn                DOUBLE,
    podn_ncl            DOUBLE,
    podn_ncu            DOUBLE,
    podn_bcl            DOUBLE,
    podn_bcu            DOUBLE,
    pofd                DOUBLE,
    pofd_ncl            DOUBLE,
    pofd_ncu            DOUBLE,
    pofd_bcl            DOUBLE,
    pofd_bcu            DOUBLE,
    far                 DOUBLE,
    far_ncl             DOUBLE,
    far_ncu             DOUBLE,
    far_bcl             DOUBLE,
    far_bcu             DOUBLE,
    csi                 DOUBLE,
    csi_ncl             DOUBLE,
    csi_ncu             DOUBLE,
    csi_bcl             DOUBLE,
    csi_bcu             DOUBLE,
    gss                 DOUBLE,
    gss_bcl             DOUBLE,
    gss_bcu             DOUBLE,
    hk                  DOUBLE,
    hk_ncl              DOUBLE,
    hk_ncu              DOUBLE,
    hk_bcl              DOUBLE,
    hk_bcu              DOUBLE,
    hss                 DOUBLE,
    hss_bcl DOUBLE,
    hss_bcu DOUBLE,
    odds DOUBLE,
    odds_ncl DOUBLE,
    odds_ncu DOUBLE,
    odds_bcl DOUBLE,
    odds_bcu DOUBLE,

    lodds DOUBLE DEFAULT -9999,
    lodds_ncl DOUBLE DEFAULT -9999,
    lodds_ncu DOUBLE DEFAULT -9999,
    lodds_bcl DOUBLE DEFAULT -9999,
    lodds_bcu DOUBLE DEFAULT -9999,

    orss DOUBLE DEFAULT -9999,
    orss_ncl DOUBLE DEFAULT -9999,
    orss_ncu DOUBLE DEFAULT -9999,
    orss_bcl DOUBLE DEFAULT -9999,
    orss_bcu DOUBLE DEFAULT -9999,

    eds DOUBLE DEFAULT -9999,
    eds_ncl DOUBLE DEFAULT -9999,
    eds_ncu DOUBLE DEFAULT -9999,
    eds_bcl DOUBLE DEFAULT -9999,
    eds_bcu DOUBLE DEFAULT -9999,

    seds DOUBLE DEFAULT -9999,
    seds_ncl DOUBLE DEFAULT -9999,
    seds_ncu DOUBLE DEFAULT -9999,
    seds_bcl DOUBLE DEFAULT -9999,
    seds_bcu DOUBLE DEFAULT -9999,

    edi DOUBLE DEFAULT -9999,
    edi_ncl DOUBLE DEFAULT -9999,
    edi_ncu DOUBLE DEFAULT -9999,
    edi_bcl DOUBLE DEFAULT -9999,
    edi_bcu DOUBLE DEFAULT -9999,

    sedi DOUBLE DEFAULT -9999,
    sedi_ncl DOUBLE DEFAULT -9999,
    sedi_ncu DOUBLE DEFAULT -9999,
    sedi_bcl DOUBLE DEFAULT -9999,
    sedi_bcu DOUBLE DEFAULT -9999,

    bagss DOUBLE DEFAULT -9999,
    bagss_bcl DOUBLE DEFAULT -9999,
    bagss_bcu DOUBLE DEFAULT -9999,

    CONSTRAINT line_data_cts_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_cts_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;
-- CREATE INDEX line_data_cts_fcst_lead_pk ON line_data_cts (fcst_lead);
-- CREATE INDEX line_data_cts_fcst_valid_beg_pk ON line_data_cts (fcst_valid_beg);
-- CREATE INDEX line_data_cts_fcst_init_beg_pk ON line_data_cts (fcst_init_beg);


-- line_data_cnt contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_cnt;
CREATE TABLE line_data_cnt
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    alpha               DOUBLE,
    total               INT UNSIGNED,
    
    fbar                DOUBLE,
    fbar_ncl            DOUBLE,
    fbar_ncu            DOUBLE,
    fbar_bcl            DOUBLE,
    fbar_bcu            DOUBLE,
    fstdev              DOUBLE,
    fstdev_ncl          DOUBLE,
    fstdev_ncu          DOUBLE,
    fstdev_bcl          DOUBLE,
    fstdev_bcu          DOUBLE,
    obar                DOUBLE,
    obar_ncl            DOUBLE,
    obar_ncu            DOUBLE,
    obar_bcl            DOUBLE,
    obar_bcu            DOUBLE,
    ostdev              DOUBLE,
    ostdev_ncl          DOUBLE,
    ostdev_ncu          DOUBLE,
    ostdev_bcl          DOUBLE,
    ostdev_bcu          DOUBLE,
    pr_corr             DOUBLE,
    pr_corr_ncl         DOUBLE,
    pr_corr_ncu         DOUBLE,
    pr_corr_bcl         DOUBLE,
    pr_corr_bcu         DOUBLE,
    sp_corr             DOUBLE,
    dt_corr             DOUBLE,
    ranks               INT UNSIGNED,
    frank_ties          INT UNSIGNED,
    orank_ties          INT UNSIGNED,
    me                  DOUBLE,
    me_ncl              DOUBLE,
    me_ncu              DOUBLE,
    me_bcl              DOUBLE,
    me_bcu              DOUBLE,
    estdev              DOUBLE,
    estdev_ncl          DOUBLE,
    estdev_ncu          DOUBLE,
    estdev_bcl          DOUBLE,
    estdev_bcu          DOUBLE,
    mbias               DOUBLE,
    mbias_bcl           DOUBLE,
    mbias_bcu           DOUBLE,
    mae                 DOUBLE,
    mae_bcl             DOUBLE,
    mae_bcu             DOUBLE,
    mse                 DOUBLE,
    mse_bcl             DOUBLE,
    mse_bcu             DOUBLE,
    bcmse               DOUBLE,
    bcmse_bcl           DOUBLE,
    bcmse_bcu           DOUBLE,
    rmse                DOUBLE,
    rmse_bcl            DOUBLE,
    rmse_bcu            DOUBLE,
    e10                 DOUBLE,
    e10_bcl             DOUBLE,
    e10_bcu             DOUBLE,
    e25                 DOUBLE,
    e25_bcl             DOUBLE,
    e25_bcu             DOUBLE,
    e50                 DOUBLE,
    e50_bcl             DOUBLE,
    e50_bcu             DOUBLE,
    e75                 DOUBLE,
    e75_bcl             DOUBLE,
    e75_bcu             DOUBLE,
    e90                 DOUBLE,
    e90_bcl             DOUBLE,
    e90_bcu             DOUBLE,
    iqr                 DOUBLE DEFAULT -9999,
    iqr_bcl             DOUBLE DEFAULT -9999,
    iqr_bcu             DOUBLE DEFAULT -9999,
    mad                 DOUBLE DEFAULT -9999,
    mad_bcl             DOUBLE DEFAULT -9999,
    mad_bcu             DOUBLE DEFAULT -9999,
    anom_corr           DOUBLE DEFAULT -9999,
    anom_corr_ncl       DOUBLE DEFAULT -9999,
    anom_corr_ncu       DOUBLE DEFAULT -9999,
    anom_corr_bcl       DOUBLE DEFAULT -9999,
    anom_corr_bcu       DOUBLE DEFAULT -9999,
    me2                 DOUBLE DEFAULT -9999,
    me2_bcl             DOUBLE DEFAULT -9999,
    me2_bcu             DOUBLE DEFAULT -9999,
    msess               DOUBLE DEFAULT -9999,
    msess_bcl           DOUBLE DEFAULT -9999,
    msess_bcu           DOUBLE DEFAULT -9999,

    
    CONSTRAINT line_data_cnt_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_cnt_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_mctc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mctc;
CREATE TABLE line_data_mctc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    n_cat               INT UNSIGNED,    
    
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_mctc_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_mctc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_mctc_cnt contains count data for a particular line_data_mctc record.  The 
--   number of counts is determined by assuming a square contingency table and stored in
--   the line_data_mctc field n_cat.

DROP TABLE IF EXISTS line_data_mctc_cnt;
CREATE TABLE line_data_mctc_cnt
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    j_value             INT UNSIGNED NOT NULL,
    fi_oj               INT UNSIGNED NOT NULL,
    
    PRIMARY KEY (line_data_id, i_value, j_value),
    CONSTRAINT line_data_mctc_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_mctc(line_data_id)
) ENGINE = MyISAM;


-- line_data_mcts contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mcts;
CREATE TABLE line_data_mcts
(
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    alpha               DOUBLE,
    total               INT UNSIGNED,
    n_cat               INT UNSIGNED,
    
    acc                 DOUBLE,
    acc_ncl             DOUBLE,
    acc_ncu             DOUBLE,
    acc_bcl             DOUBLE,
    acc_bcu             DOUBLE,
    hk                  DOUBLE,
    hk_bcl              DOUBLE,
    hk_bcu              DOUBLE,
    hss                 DOUBLE,
    hss_bcl             DOUBLE,
    hss_bcu             DOUBLE,
    ger                 DOUBLE,
    ger_bcl             DOUBLE,
    ger_bcu             DOUBLE,
    
    CONSTRAINT line_data_mcts_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_mcts_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_pct contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_pct;
CREATE TABLE line_data_pct
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pct_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_pct_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_pct_thresh contains threshold data for a particular line_data_pct record and
--   threshold.  The number of thresholds stored is given by the line_data_pct field n_thresh.

DROP TABLE IF EXISTS line_data_pct_thresh;
CREATE TABLE line_data_pct_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    oy_i                INT UNSIGNED,
    on_i                INT UNSIGNED,
    
    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_pct_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_pct(line_data_id)
) ENGINE = MyISAM;


-- line_data_pstd contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_pstd;
CREATE TABLE line_data_pstd
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    alpha               DOUBLE,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    
    baser               DOUBLE,
    baser_ncl           DOUBLE,
    baser_ncu           DOUBLE,
    reliability         DOUBLE,
    resolution          DOUBLE,
    uncertainty         DOUBLE,
    roc_auc             DOUBLE,
    brier               DOUBLE,
    brier_ncl           DOUBLE,
    brier_ncu           DOUBLE,

    briercl            DOUBLE DEFAULT -9999,
    briercl_ncl        DOUBLE DEFAULT -9999,
    briercl_ncu        DOUBLE DEFAULT -9999,
    bss                 DOUBLE DEFAULT -9999,


    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pstd_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_pstd_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_pstd_thresh contains threshold data for a particular line_data_pstd record and
--   threshold.  The number of thresholds stored is given by the line_data_pstd field n_thresh.

DROP TABLE IF EXISTS line_data_pstd_thresh;
CREATE TABLE line_data_pstd_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    
    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_pstd_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_pstd(line_data_id)
) ENGINE = MyISAM;


-- line_data_pjc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_pjc;
CREATE TABLE line_data_pjc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pjc_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_pjc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_pjc_thresh contains threshold data for a particular line_data_pjc record and
--   threshold.  The number of thresholds stored is given by the line_data_pjc field n_thresh.

DROP TABLE IF EXISTS line_data_pjc_thresh;
CREATE TABLE line_data_pjc_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    oy_tp_i             DOUBLE,
    on_tp_i             DOUBLE,
    calibration_i       DOUBLE,
    refinement_i        DOUBLE,
    likelihood_i        DOUBLE,
    baser_i             DOUBLE,
    
    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_pjc_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_pjc(line_data_id)
) ENGINE = MyISAM;


-- line_data_prc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_prc;
CREATE TABLE line_data_prc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_prc_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_prc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_prc_thresh contains threshold data for a particular line_data_prc record and
--   threshold.  The number of thresholds stored is given by the line_data_prc field n_thresh.

DROP TABLE IF EXISTS line_data_prc_thresh;
CREATE TABLE line_data_prc_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    pody_i              DOUBLE,
    pofd_i              DOUBLE,
    
    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_prc_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_prc(line_data_id)
) ENGINE = MyISAM;


-- line_data_sl1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_sl1l2;
CREATE TABLE line_data_sl1l2
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    fbar                DOUBLE,
    obar                DOUBLE,
    fobar               DOUBLE,
    ffbar               DOUBLE,
    oobar               DOUBLE,
    mae                 DOUBLE DEFAULT -9999,
           
    CONSTRAINT line_data_sl1l2_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_sl1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_sal1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_sal1l2;
CREATE TABLE line_data_sal1l2
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    fabar               DOUBLE,
    oabar               DOUBLE,
    foabar              DOUBLE,
    ffabar              DOUBLE,
    ooabar              DOUBLE,
    mae                 DOUBLE DEFAULT -9999,
    
    CONSTRAINT line_data_sal2l1_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_sal2l1_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_vl1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_vl1l2;
CREATE TABLE line_data_vl1l2
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    ufbar               DOUBLE,
    vfbar               DOUBLE,
    uobar               DOUBLE,
    vobar               DOUBLE,
    uvfobar             DOUBLE,
    uvffbar             DOUBLE,
    uvoobar             DOUBLE,
    
    CONSTRAINT line_data_vl1l2_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_vl1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_val1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_val1l2;
CREATE TABLE line_data_val1l2
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    ufabar              DOUBLE,
    vfabar              DOUBLE,
    uoabar              DOUBLE,
    voabar              DOUBLE,
    uvfoabar            DOUBLE,
    uvffabar            DOUBLE,
    uvooabar            DOUBLE,
    
    CONSTRAINT line_data_val1l2_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_val1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_mpr contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mpr;
CREATE TABLE line_data_mpr
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    mp_index            INT UNSIGNED,
    obs_sid             VARCHAR(32),
    obs_lat             DOUBLE,
    obs_lon             DOUBLE,
    obs_lvl             DOUBLE,
    obs_elv             DOUBLE,
    mpr_fcst               DOUBLE,
    mpr_obs                 DOUBLE,
    mpr_climo               DOUBLE,
    obs_qc              DOUBLE DEFAULT -9999,
    
    CONSTRAINT line_data_mpr_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_mpr_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_nbrctc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_nbrctc;
CREATE TABLE line_data_nbrctc
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    cov_thresh          VARCHAR(32),
    total               INT UNSIGNED,
    
    fy_oy               INT UNSIGNED,
    fy_on               INT UNSIGNED,
    fn_oy               INT UNSIGNED,
    fn_on               INT UNSIGNED,
    
    CONSTRAINT line_data_nbrctc_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_nbrctc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_nbrcts contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_nbrcts;
CREATE TABLE line_data_nbrcts
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    cov_thresh          VARCHAR(32),
    alpha               DOUBLE,
    total               INT UNSIGNED,
    
    baser               DOUBLE,
    baser_ncl           DOUBLE,
    baser_ncu           DOUBLE,
    baser_bcl           DOUBLE,
    baser_bcu           DOUBLE,
    fmean               DOUBLE,
    fmean_ncl           DOUBLE,
    fmean_ncu           DOUBLE,
    fmean_bcl           DOUBLE,
    fmean_bcu           DOUBLE,
    acc                 DOUBLE,
    acc_ncl             DOUBLE,
    acc_ncu             DOUBLE,
    acc_bcl             DOUBLE,
    acc_bcu             DOUBLE,
    fbias               DOUBLE,
    fbias_bcl           DOUBLE,
    fbias_bcu           DOUBLE,
    pody                DOUBLE,
    pody_ncl            DOUBLE,
    pody_ncu            DOUBLE,
    pody_bcl            DOUBLE,
    pody_bcu            DOUBLE,
    podn                DOUBLE,
    podn_ncl            DOUBLE,
    podn_ncu            DOUBLE,
    podn_bcl            DOUBLE,
    podn_bcu            DOUBLE,
    pofd                DOUBLE,
    pofd_ncl            DOUBLE,
    pofd_ncu            DOUBLE,
    pofd_bcl            DOUBLE,
    pofd_bcu            DOUBLE,
    far                 DOUBLE,
    far_ncl             DOUBLE,
    far_ncu             DOUBLE,
    far_bcl             DOUBLE,
    far_bcu             DOUBLE,
    csi                 DOUBLE,
    csi_ncl             DOUBLE,
    csi_ncu             DOUBLE,
    csi_bcl             DOUBLE,
    csi_bcu             DOUBLE,
    gss                 DOUBLE,
    gss_bcl             DOUBLE,
    gss_bcu             DOUBLE,
    hk                  DOUBLE,
    hk_ncl              DOUBLE,
    hk_ncu              DOUBLE,
    hk_bcl              DOUBLE,
    hk_bcu              DOUBLE,
    hss                 DOUBLE,
    hss_bcl             DOUBLE,
  hss_bcu DOUBLE,
  odds DOUBLE,
  odds_ncl DOUBLE,
  odds_ncu DOUBLE,
  odds_bcl DOUBLE,
  odds_bcu DOUBLE,

  lodds DOUBLE DEFAULT -9999,
  lodds_ncl DOUBLE DEFAULT -9999,
  lodds_ncu DOUBLE DEFAULT -9999,
  lodds_bcl DOUBLE DEFAULT -9999,
  lodds_bcu DOUBLE DEFAULT -9999,

  orss DOUBLE DEFAULT -9999,
  orss_ncl DOUBLE DEFAULT -9999,
  orss_ncu DOUBLE DEFAULT -9999,
  orss_bcl DOUBLE DEFAULT -9999,
  orss_bcu DOUBLE DEFAULT -9999,

  eds DOUBLE DEFAULT -9999,
  eds_ncl DOUBLE DEFAULT -9999,
  eds_ncu DOUBLE DEFAULT -9999,
  eds_bcl DOUBLE DEFAULT -9999,
  eds_bcu DOUBLE DEFAULT -9999,

  seds DOUBLE DEFAULT -9999,
  seds_ncl DOUBLE DEFAULT -9999,
  seds_ncu DOUBLE DEFAULT -9999,
  seds_bcl DOUBLE DEFAULT -9999,
  seds_bcu DOUBLE DEFAULT -9999,

  edi DOUBLE DEFAULT -9999,
  edi_ncl DOUBLE DEFAULT -9999,
  edi_ncu DOUBLE DEFAULT -9999,
  edi_bcl DOUBLE DEFAULT -9999,
  edi_bcu DOUBLE DEFAULT -9999,

  sedi DOUBLE DEFAULT -9999,
  sedi_ncl DOUBLE DEFAULT -9999,
  sedi_ncu DOUBLE DEFAULT -9999,
  sedi_bcl DOUBLE DEFAULT -9999,
  sedi_bcu DOUBLE DEFAULT -9999,

  bagss DOUBLE DEFAULT -9999,
  bagss_bcl DOUBLE DEFAULT -9999,
  bagss_bcu DOUBLE DEFAULT -9999,
    
    CONSTRAINT line_data_nbrcts_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_nbrcts_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_nbrcnt contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_nbrcnt;
CREATE TABLE line_data_nbrcnt
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    alpha               DOUBLE,
    total               INT UNSIGNED,
    
    fbs                 DOUBLE,
    fbs_bcl             DOUBLE,
    fbs_bcu             DOUBLE,
    fss                 DOUBLE,
    fss_bcl DOUBLE,
    fss_bcu DOUBLE,
    afss DOUBLE DEFAULT -9999,
    afss_bcl DOUBLE DEFAULT -9999,
    afss_bcu DOUBLE DEFAULT -9999,
    ufss DOUBLE DEFAULT -9999,
    ufss_bcl DOUBLE DEFAULT -9999,
    ufss_bcu DOUBLE DEFAULT -9999,
    f_rate DOUBLE DEFAULT -9999,
    f_rate_bcl DOUBLE DEFAULT -9999,
    f_rate_bcu DOUBLE DEFAULT -9999,
    o_rate DOUBLE DEFAULT -9999,
    o_rate_bcl DOUBLE DEFAULT -9999,
    o_rate_bcu DOUBLE DEFAULT -9999,


  CONSTRAINT line_data_nbrcnt_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_nbrcnt_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;

DROP TABLE IF EXISTS line_data_enscnt;
CREATE TABLE line_data_enscnt
(
  stat_header_id INT UNSIGNED NOT NULL,
   data_file_id INT UNSIGNED NOT NULL,
   line_num INT UNSIGNED,
   fcst_lead INT UNSIGNED,
   fcst_valid_beg DATETIME,
   fcst_valid_end DATETIME,
   fcst_init_beg DATETIME,
   obs_lead INT UNSIGNED,
   obs_valid_beg DATETIME,
   obs_valid_end DATETIME,

   rpsf DOUBLE DEFAULT -9999,
   rpsf_ncl DOUBLE DEFAULT -9999,
   rpsf_ncu DOUBLE DEFAULT -9999,
   rpsf_bcl DOUBLE DEFAULT -9999,
   rpsf_bcu DOUBLE DEFAULT -9999,

   rpscl DOUBLE DEFAULT -9999,
   rpscl_ncl DOUBLE DEFAULT -9999,
   rpscl_ncu DOUBLE DEFAULT -9999,
   rpscl_bcl DOUBLE DEFAULT -9999,
   rpscl_bcu DOUBLE DEFAULT -9999,

   rpss DOUBLE DEFAULT -9999,
   rpss_ncl DOUBLE DEFAULT -9999,
   rpss_ncu DOUBLE DEFAULT -9999,
   rpss_bcl DOUBLE DEFAULT -9999,
   rpss_bcu DOUBLE DEFAULT -9999,

   crpsf DOUBLE DEFAULT -9999,
   crpsf_ncl DOUBLE DEFAULT -9999,
   crpsf_ncu DOUBLE DEFAULT -9999,
   crpsf_bcl DOUBLE DEFAULT -9999,
   crpsf_bcu DOUBLE DEFAULT -9999,

   crpscl DOUBLE DEFAULT -9999,
   crpscl_ncl DOUBLE DEFAULT -9999,
   crpscl_ncu DOUBLE DEFAULT -9999,
   crpscl_bcl DOUBLE DEFAULT -9999,
   crpscl_bcu DOUBLE DEFAULT -9999,

   crpss DOUBLE DEFAULT -9999,
   crpss_ncl DOUBLE DEFAULT -9999,
   crpss_ncu DOUBLE DEFAULT -9999,
   crpss_bcl DOUBLE DEFAULT -9999,
   crpss_bcu DOUBLE DEFAULT -9999,



   CONSTRAINT line_data_enscnt_file_id_pk
   FOREIGN KEY (data_file_id)
   REFERENCES data_file (data_file_id),
   CONSTRAINT line_data_enscnt_stat_header_id_pk
   FOREIGN KEY (stat_header_id)
   REFERENCES stat_header (stat_header_id),
       INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;



--  contains stat data for a particular stat_header record, which it points
--    at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_isc;
CREATE TABLE line_data_isc
(
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    tile_dim            DOUBLE,
    time_xll            DOUBLE,
    tile_yll            DOUBLE,
    nscale              DOUBLE,
    iscale              DOUBLE,
    mse                 DOUBLE,
    isc                 DOUBLE,
    fenergy2            DOUBLE,
    oenergy2            DOUBLE,
    baser               DOUBLE,
    fbias               DOUBLE,
    
    CONSTRAINT line_data_isc_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_isc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_rhist contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_rhist;
CREATE TABLE line_data_rhist
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    crps                DOUBLE,
    ign                 DOUBLE,
    n_rank              INT UNSIGNED,
    crpss               DOUBLE,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_rhist_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_rhist_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_rhist_rank contains rank data for a particular line_data_rhist record.  The 
--   number of ranks stored is given by the line_data_rhist field n_rank.

DROP TABLE IF EXISTS line_data_rhist_rank;
CREATE TABLE line_data_rhist_rank
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    rank_i              INT UNSIGNED,
    
    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_rhist_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_rhist(line_data_id)
) ENGINE = MyISAM;

-- line_data_phist contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_phist;
CREATE TABLE line_data_phist
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,

    bin_size            DOUBLE,
    n_bin              INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_phist_data_file_id_pk FOREIGN KEY(data_file_id) REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_phist_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_phist_rank contains rank data for a particular line_data_phist record.  The
--   number of ranks stored is given by the line_data_phist field n_rank.

DROP TABLE IF EXISTS line_data_phist_bin;
CREATE TABLE line_data_phist_bin
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    bin_i              INT UNSIGNED,

    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_phist_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_phist(line_data_id)
) ENGINE = MyISAM;



-- line_data_orank contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_orank;
CREATE TABLE line_data_orank
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    total               INT UNSIGNED,
    
    orank_index         INT UNSIGNED,
    obs_sid             VARCHAR(64),
    obs_lat             VARCHAR(64),
    obs_lon             VARCHAR(64),
    obs_lvl             VARCHAR(64),
    obs_elv             VARCHAR(64),
    orank_obs                 DOUBLE,
    pit                 DOUBLE,
    rank                INT UNSIGNED,
    n_ens_vld           INT UNSIGNED,
    n_ens               INT UNSIGNED,
    obs_qc              DOUBLE DEFAULT -9999,
    ens_mean            DOUBLE DEFAULT -9999,
    orank_climo               DOUBLE DEFAULT -9999,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_orank_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_orank_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;


-- line_data_orank_ens contains ensemble data for a particular line_data_orank record.  The 
--   number of ens values stored is given by the line_data_orank field n_ens.

DROP TABLE IF EXISTS line_data_orank_ens;
CREATE TABLE line_data_orank_ens
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    ens_i               DOUBLE,
    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_orank_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_orank(line_data_id)
) ENGINE = MyISAM;


-- line_data_nbrcnt contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_ssvar;
CREATE TABLE line_data_ssvar
(
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    fcst_lead           INT,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    alpha               DOUBLE,
    total               INT UNSIGNED,

    n_bin               INT UNSIGNED,
    bin_i               INT UNSIGNED,
    bin_n               INT UNSIGNED,
    var_min             DOUBLE,
    var_max             DOUBLE,
    var_mean            DOUBLE,
    fbar                DOUBLE,
    obar                DOUBLE,
    fobar               DOUBLE,
    ffbar               DOUBLE,
    oobar               DOUBLE,

    fbar_ncl            DOUBLE,
    fbar_ncu            DOUBLE,
    fstdev              DOUBLE,
    fstdev_ncl          DOUBLE,
    fstdev_ncu          DOUBLE,
    obar_ncl            DOUBLE,
    obar_ncu            DOUBLE,
    ostdev              DOUBLE,
    ostdev_ncl          DOUBLE,
    ostdev_ncu          DOUBLE,
    pr_corr             DOUBLE,
    pr_corr_ncl         DOUBLE,
    pr_corr_ncu         DOUBLE,
    me	                DOUBLE,
    me_ncl              DOUBLE,
    me_ncu              DOUBLE,
    estdev              DOUBLE,
    estdev_ncl          DOUBLE,
    estdev_ncu          DOUBLE,
    mbias               DOUBLE,
    mse                 DOUBLE,
    bcmse               DOUBLE,
    rmse	              DOUBLE,
    CONSTRAINT line_data_ssvar_data_file_id_pk
            FOREIGN KEY(data_file_id)
            REFERENCES data_file(data_file_id),
    CONSTRAINT line_data_ssvar_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id),
                INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM;




-- mode_header represents a line in a mode file and contains the header information for
--   that line.  The line-dependent information is stored in specific tables for each line 
--   type, each of which point at the line they are associated with, via the mode_header_id 
--   field.  Each mode_header line also specifies what type it is by pointing at a line
--   type in the line_type_lu table, via the line_type_lu_id field.  The file that the
--   line information was stored in is specified by a record in the data_file table, pointed
--   at by the data_file_id field.

DROP TABLE IF EXISTS mode_header;
CREATE TABLE mode_header
(
    mode_header_id      INT UNSIGNED NOT NULL,
    line_type_lu_id     INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    linenumber          INT UNSIGNED,
    version             VARCHAR(8),
    model               VARCHAR(64),
    fcst_lead           INT,
    fcst_valid          DATETIME,
    fcst_accum          INT UNSIGNED,
    fcst_init           DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid           DATETIME,
    obs_accum           INT UNSIGNED,
    fcst_rad            INT UNSIGNED,
    fcst_thr            VARCHAR(16),
    obs_rad             INT UNSIGNED,
    obs_thr             VARCHAR(16),
    fcst_var            VARCHAR(64),
    fcst_lev            VARCHAR(16),
    obs_var             VARCHAR(64),
    obs_lev             VARCHAR(16),
    PRIMARY KEY (mode_header_id),

    CONSTRAINT mode_header_data_file_id_pk
        FOREIGN KEY(data_file_id)
        REFERENCES data_file(data_file_id),
    CONSTRAINT stat_header_unique_pk
        UNIQUE INDEX (
            model,
            fcst_lead,
            fcst_valid,
            fcst_accum,
            fcst_init,
            obs_lead,
            obs_valid,
            obs_accum,
            fcst_rad,
            fcst_thr,
            obs_rad,
            obs_thr,
            fcst_var,
            fcst_lev,
            obs_var,
            obs_lev
        )
) ENGINE = MyISAM;


-- mode_cts contains mode cts data for a particular mode_header record, which it points 
--   at via the mode_header_id field.

DROP TABLE IF EXISTS mode_cts;
CREATE TABLE mode_cts
(
    mode_header_id      INT UNSIGNED NOT NULL,
    field               VARCHAR(16),
    total               INT UNSIGNED,
    fy_oy               INT UNSIGNED,
    fy_on               INT UNSIGNED,
    fn_oy               INT UNSIGNED,
    fn_on               INT UNSIGNED,
    baser               DOUBLE,
    fmean               DOUBLE,
    acc                 DOUBLE,
    fbias               DOUBLE,
    pody                DOUBLE,
    podn                DOUBLE,
    pofd                DOUBLE,
    far                 DOUBLE,
    csi                 DOUBLE,
    gss                 DOUBLE,
    hk                  DOUBLE,
    hss                 DOUBLE,
    odds                DOUBLE,
    CONSTRAINT mode_cts_mode_header_id_pk
        FOREIGN KEY(mode_header_id)
        REFERENCES mode_header(mode_header_id)
) ENGINE = MyISAM;


-- mode_obj_single contains mode object data for a particular mode_header record, which it 
--   points at via the mode_header_id field.  This table stores information only about 
--   single mode objects.  Mode object pair information is stored in the mode_obj_pair 
--   table.

DROP TABLE IF EXISTS mode_obj_single;
CREATE TABLE mode_obj_single
(
    mode_obj_id         INT UNSIGNED NOT NULL,
    mode_header_id      INT UNSIGNED NOT NULL,
    object_id           VARCHAR(128),
    object_cat          VARCHAR(128),
    centroid_x          DOUBLE,
    centroid_y          DOUBLE,
    centroid_lat        DOUBLE,
    centroid_lon        DOUBLE,
    axis_avg            DOUBLE,
    length              DOUBLE,
    width               DOUBLE,
    area                INT UNSIGNED,
    area_filter         INT UNSIGNED,
    area_thresh         INT UNSIGNED,
    curvature           DOUBLE,
    curvature_x         DOUBLE,
    curvature_y         DOUBLE,
    complexity          DOUBLE,
    intensity_10        DOUBLE,
    intensity_25        DOUBLE,
    intensity_50        DOUBLE,
    intensity_75        DOUBLE,
    intensity_90        DOUBLE,
    intensity_nn        DOUBLE,
    intensity_sum       DOUBLE,
    PRIMARY KEY (mode_obj_id),
    CONSTRAINT mode_obj_single_mode_header_id_pk
            FOREIGN KEY(mode_header_id)
            REFERENCES mode_header(mode_header_id)
) ENGINE = MyISAM;


-- mode_obj_pair contains mode object data for a particular mode_header record, which it 
--   points at via the mode_header_id field.  This table stores information only about pairs
--   of mode objects.  Each mode_obj_pair record points at two mode_obj_single records, one
--   corresponding to the observed object (via mode_obj_obs) and one corresponding to the 
--   forecast object (via mode_obj_fcst). 

DROP TABLE IF EXISTS mode_obj_pair;
CREATE TABLE mode_obj_pair
(
    mode_obj_obs_id     INT UNSIGNED NOT NULL,
    mode_obj_fcst_id    INT UNSIGNED NOT NULL,
    mode_header_id      INT UNSIGNED NOT NULL,    
    object_id           VARCHAR(128),
    object_cat          VARCHAR(128),
    centroid_dist       DOUBLE,
    boundary_dist       DOUBLE,
    convex_hull_dist    DOUBLE,
    angle_diff          DOUBLE,
    area_ratio          DOUBLE,
    intersection_area   INT UNSIGNED,
    union_area          INT UNSIGNED,
    symmetric_diff      INTEGER,
    intersection_over_area DOUBLE,
    complexity_ratio    DOUBLE,
    percentile_intensity_ratio DOUBLE,
    interest            DOUBLE,
    CONSTRAINT mode_obj_pair_mode_header_id_pk
        FOREIGN KEY(mode_header_id)
        REFERENCES mode_header(mode_header_id),
    CONSTRAINT mode_obj_pair_mode_obj_obs_pk
        FOREIGN KEY(mode_obj_obs_id)
        REFERENCES mode_obj_single(mode_obj_id),
    CONSTRAINT mode_obj_pair_mode_obj_fcst_pk
        FOREIGN KEY(mode_obj_fcst_id)
        REFERENCES mode_obj_single(mode_obj_id)
) ENGINE = MyISAM;


--  look-up table data

INSERT INTO data_file_lu VALUES (0, 'point_stat', 'Verification statistics for forecasts at observation points');
INSERT INTO data_file_lu VALUES (1, 'grid_stat', 'Verification statistics for a matched forecast and observation grid');
INSERT INTO data_file_lu VALUES (2, 'mode_cts', 'Contingency table counts and statistics comparing forecast and observations');
INSERT INTO data_file_lu VALUES (3, 'mode_obj', 'Attributes for simple objects, merged cluster objects and pairs of objects');
INSERT INTO data_file_lu VALUES (4, 'wavelet_stat', 'Verification statistics for intensity-scale comparison of forecast and observations');
INSERT INTO data_file_lu VALUES (5, 'ensemble_stat', 'Ensemble verification statistics');
INSERT INTO data_file_lu VALUES (6, 'vsdb_point_stat', 'Verification statistics for forecasts at observation points for vsdb files');
INSERT INTO data_file_lu VALUES (7, 'stat', 'All verification statistics');


-- mv_rev contains information about metvdb revisions, and provides an indicator of
--   the changes made in the current revision

DROP TABLE IF EXISTS mv_rev;
CREATE TABLE mv_rev
(
    rev_id              INT UNSIGNED NOT NULL,
    rev_date            DATETIME,
    rev_name            VARCHAR(16),
    rev_detail          VARCHAR(2048),
    PRIMARY KEY (rev_id)    
) ENGINE = MyISAM;

INSERT INTO mv_rev VALUES (0, '2010-07-29 12:00:00', '0.1',   'Initial revision, includes metvdb_rev, instance_info and web_plot tables');
INSERT INTO mv_rev VALUES (1, '2010-10-14 12:00:00', '0.1',   'Increased web_plot.plot_xml field width to 65536');
INSERT INTO mv_rev VALUES (2, '2010-11-15 12:00:00', '0.3',   'METViewer changes to support out from METv3.0');
INSERT INTO mv_rev VALUES (3, '2011-01-13 12:00:00', '0.5',   'Major refactoring of schema, compatible with METv3.0');
INSERT INTO mv_rev VALUES (4, '2011-03-18 12:00:00', '0.5',   'Added instance_info table');
INSERT INTO mv_rev VALUES (5, '2012-09-25 12:00:00', '0.5.6', 'Added line_data_ssvar table');


-- instance_info contains information about the particular instance of metvdb, including 
--   dates of data updates and information about data table contents

CREATE TABLE IF NOT EXISTS instance_info
(
    instance_info_id    INT UNSIGNED NOT NULL,
    updater             VARCHAR(64),
    update_date         DATETIME,
    update_detail       VARCHAR(2048),
    load_xml            TEXT,
    PRIMARY KEY (instance_info_id)    
) ENGINE = MyISAM;


CREATE TABLE model_fcst_lead_offset
(
  model VARCHAR(64),
  fcst_lead_offset INT,
  PRIMARY KEY (model)
) ENGINE = MyISAM ;


-- change the delimiter for the process of declaring functions

DELIMITER |

--
--  Sl1l2 stat calculations
--

DROP FUNCTION IF EXISTS calcStdDev |
CREATE FUNCTION calcStdDev (vsum REAL, vsum_sq REAL, n INT) RETURNS REAL DETERMINISTIC
BEGIN
    DECLARE v DECIMAL(12,6);
    IF 1 > n THEN RETURN -1; END IF;
    SET v = (vsum_sq - vsum*vsum/n)/(n - 1);
    IF 0 > v THEN RETURN -1; END IF;
    RETURN SQRT(v);
END |

DROP FUNCTION IF EXISTS calcFBAR |
CREATE FUNCTION calcFBAR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN RETURN IFNULL(fbar, 'NA'); END |

DROP FUNCTION IF EXISTS calcOBAR |
CREATE FUNCTION calcOBAR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN RETURN  IFNULL(obar, 'NA'); END |

DROP FUNCTION IF EXISTS calcFSTDEV |
CREATE FUNCTION calcFSTDEV (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN RETURN calcStdDev(fbar * total, ffbar * total, total); END |

DROP FUNCTION IF EXISTS calcOSTDEV |
CREATE FUNCTION calcOSTDEV (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    RETURN calcStdDev(obar * total, oobar * total, total);
END |

DROP FUNCTION IF EXISTS calcFOBAR |
CREATE FUNCTION calcFOBAR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN RETURN  IFNULL( fobar, 'NA'); END |

DROP FUNCTION IF EXISTS calcFFBAR |
CREATE FUNCTION calcFFBAR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN RETURN  IFNULL(ffbar, 'NA'); END |

DROP FUNCTION IF EXISTS calcOOBAR |
CREATE FUNCTION calcOOBAR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN RETURN  IFNULL(oobar, 'NA'); END |

DROP FUNCTION IF EXISTS calcMBIAS |
CREATE FUNCTION calcMBIAS (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF 0 = obar THEN RETURN 'NA'; END IF;
    SET result =(fbar / obar);
    RETURN  IFNULL( result, 'NA'); END |

DROP FUNCTION IF EXISTS calcPR_CORR |
CREATE FUNCTION calcPR_CORR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE v DECIMAL(12,6);
    DECLARE pr_corr DECIMAL(12,6);
    SET v = (POW(total,2) * ffbar - POW(total,2) * POW(fbar,2)) * (POW(total,2) * oobar - POW(total,2) * POW(obar,2));
    IF 0 >= v THEN RETURN 'NA'; END IF;
    SET pr_corr = (POW(total,2) * fobar - POW(total,2) * fbar * obar) / SQRT(v);
    IF 1 < pr_corr THEN RETURN 'NA'; END IF;
    RETURN IFNULL( pr_corr, 'NA') ;
END |

DROP FUNCTION IF EXISTS calcANOM_CORR |
CREATE FUNCTION calcANOM_CORR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE v DECIMAL(12,6);
    DECLARE anom_corr DECIMAL(12,6);
    SET v = (POW(total,2) * ffbar - POW(total,2) * POW(fbar,2)) * (POW(total,2) * oobar - POW(total,2) * POW(obar,2));
    IF 0 >= v THEN RETURN 'NA'; END IF;
    SET anom_corr = (POW(total,2) * fobar - POW(total,2) * fbar * obar) / SQRT(v);
    IF 1 < anom_corr THEN RETURN 'NA'; END IF;
    RETURN IFNULL(anom_corr, 'NA');
END |


DROP FUNCTION IF EXISTS calcME |
CREATE FUNCTION calcME (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    RETURN  IFNULL((fbar - obar), 'NA') ;
END |

DROP FUNCTION IF EXISTS calcME2 |
CREATE FUNCTION calcME2 (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE me DECIMAL(12,6);
    SET me = calcME(total , fbar , obar , fobar , ffbar , oobar );
RETURN  IFNULL((me * me), 'NA'); END |

DROP FUNCTION IF EXISTS calcMSE |
CREATE FUNCTION calcMSE (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    RETURN IFNULL((ffbar + oobar - 2*fobar), 'NA');
END |

DROP FUNCTION IF EXISTS calcMSESS |
CREATE FUNCTION calcMSESS (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE ostdev DECIMAL(12,6);
    DECLARE mse DECIMAL(12,6);
    SET ostdev=calcOSTDEV(total , fbar , obar , fobar , ffbar , oobar );
    SET mse=calcMSE(total , fbar , obar , fobar , ffbar , oobar );
RETURN IFNULL( (1.0 - mse/(ostdev*ostdev) ), 'NA'); END |

DROP FUNCTION IF EXISTS calcRMSE |
CREATE FUNCTION calcRMSE (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE a DECIMAL(12,6);
    SET a = ffbar + oobar - 2*fobar;
    RETURN  IFNULL(SQRT(a), 'NA');
END |

DROP FUNCTION IF EXISTS calcESTDEV |
CREATE FUNCTION calcESTDEV (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    DECLARE a DECIMAL(12,6);
    DECLARE b DECIMAL(12,6);
    SET a = (fbar - obar)*total;
    SET b = (ffbar + oobar - 2*fobar)*total;
    SET result = calcStdDev(a , b , total );
    RETURN  result;
END |

DROP FUNCTION IF EXISTS calcBCMSE |
CREATE FUNCTION calcBCMSE (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    SET result = (ffbar + oobar - 2*fobar) - POW(fbar - obar, 2);
    RETURN  IFNULL( result, 'NA');
END |

DROP FUNCTION IF EXISTS calcBCRMSE |
CREATE FUNCTION calcBCRMSE (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    SET result = SQRT((ffbar + oobar - 2*fobar) - POW(fbar - obar, 2));
    RETURN  IFNULL(result , 'NA');
END |

DROP FUNCTION IF EXISTS calcMAE |
CREATE FUNCTION calcMAE ( mae REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN IF mae = -9999 THEN RETURN 'NA'; END IF; RETURN  mae; END |

--
-- CTC stat calculations
--

DROP FUNCTION IF EXISTS calcBASER |
CREATE FUNCTION calcBASER (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF total = 0 THEN RETURN 'NA'; END IF;
    SET result = (fy_oy + fn_oy) / total;
    RETURN IFNULL(result , 'NA');
END |

DROP FUNCTION IF EXISTS calcACC |
CREATE FUNCTION calcACC (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF total = 0 THEN RETURN 'NA'; END IF;
    SET result = (fy_oy + fn_on) / total;
    RETURN IFNULL(result, 'NA');
END |

DROP FUNCTION IF EXISTS calcFBIAS |
CREATE FUNCTION calcFBIAS (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF (fy_oy + fn_oy) = 0 THEN RETURN 'NA'; END IF;
    SET result = (fy_oy + fy_on) / (fy_oy + fn_oy);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcPODY |
CREATE FUNCTION calcPODY (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF (fy_oy + fn_oy) = 0 THEN RETURN 'NA'; END IF;
    SET result = fy_oy / (fy_oy + fn_oy);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcPOFD |
CREATE FUNCTION calcPOFD (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF (fy_on + fn_on) = 0 THEN RETURN 'NA'; END IF;
    SET result = fy_on / (fy_on + fn_on);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcPODN |
CREATE FUNCTION calcPODN (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF (fy_on + fn_on) = 0 THEN RETURN 'NA'; END IF;
    SET result = fn_on / (fy_on + fn_on);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcFAR |
CREATE FUNCTION calcFAR (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF (fy_oy + fy_on) = 0 THEN RETURN 'NA'; END IF;
    SET result = fy_on / (fy_oy + fy_on);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcCSI |
CREATE FUNCTION calcCSI (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF (fy_oy + fy_on + fn_oy) = 0 THEN RETURN 'NA'; END IF;
    SET result = fy_oy / (fy_oy + fy_on + fn_oy);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcGSS |
CREATE FUNCTION calcGSS (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE c DECIMAL(12,6);
    DECLARE result DECIMAL(12,6);
    IF total = 0 THEN RETURN 'NA'; END IF;
    SET c = ( (fy_oy + fy_on) / total ) * (fy_oy + fn_oy);
    SET result = (fy_oy - c) / (fy_oy + fy_on + fn_oy - c);
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcHK |
CREATE FUNCTION calcHK (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE result DECIMAL(12,6);
    IF ( (fy_oy + fn_oy) = 0 OR (fy_on + fn_on) = 0 ) THEN RETURN 'NA'; END IF;
    SET result = (fy_oy / (fy_oy + fn_oy)) - (fy_on / (fy_on + fn_on));
    RETURN IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcHSS |
CREATE FUNCTION calcHSS (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE c DECIMAL(12,6);
    DECLARE result DECIMAL(12,6);
    IF total = 0 THEN RETURN 'NA'; END IF;
    SET c = ( (fy_oy + fy_on)*(fy_oy + fn_oy) + (fn_oy + fn_on)*(fy_on + fn_on) ) / total;
    SET result = (fy_oy + fn_on - c) / (total - c);
    RETURN  IFNULL( result , 'NA');
END |

DROP FUNCTION IF EXISTS calcODDS |
CREATE FUNCTION calcODDS (total INT, fy_oy INT, fy_on INT, fn_oy INT, fn_on INT) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE pody DECIMAL(12,6);
    DECLARE pofd DECIMAL(12,6);
    DECLARE result DECIMAL(12,6);
    IF ( (fy_oy + fn_oy) = 0 OR (fy_on + fn_on) = 0 ) THEN RETURN 'NA'; END IF;
    SET pody = fy_oy / (fy_oy + fn_oy);
    SET pofd = fy_on / (fy_on + fn_on);
    IF ( pody = 0 OR pofd = 0 ) THEN RETURN 'NA'; END IF;
    SET result = (pody * (1-pofd)) / (pofd * (1-pody));
    RETURN  IFNULL(result , 'NA');
END |

DROP FUNCTION IF EXISTS calcME2 |
CREATE FUNCTION calcME2 (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
  DECLARE me DECIMAL(12,6);
  DECLARE result DECIMAL(12,6);
  SET me = calcME(total , fbar , obar , fobar , ffbar , oobar );
  SET result = (me * me);
RETURN  IFNULL(result, 'NA'); END |

DROP FUNCTION IF EXISTS calcMSESS |
CREATE FUNCTION calcMSESS (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE ostdev DECIMAL(12,6);
    DECLARE mse DECIMAL(12,6);
    DECLARE result DECIMAL(12,6);
    SET ostdev=calcOSTDEV(total , fbar , obar , fobar , ffbar , oobar );
    SET mse=calcMSE(total , fbar , obar , fobar , ffbar , oobar );
    SET result = (1.0 - mse/(ostdev*ostdev) );
RETURN IFNULL( result ,'NA'); END |

DROP FUNCTION IF EXISTS calcANOM_CORR |
CREATE FUNCTION calcANOM_CORR (total INT, fbar REAL, obar REAL, fobar REAL, ffbar REAL, oobar REAL) RETURNS CHAR(16) DETERMINISTIC
BEGIN
    DECLARE v DECIMAL(12,6);
    DECLARE anom_corr DECIMAL(12,6);
    SET v = (POW(total,2) * ffbar - POW(total,2) * POW(fbar,2)) * (POW(total,2) * oobar - POW(total,2) * POW(obar,2));
    IF 0 >= v THEN RETURN 'NA'; END IF;
    SET anom_corr = (POW(total,2) * fobar - POW(total,2) * fbar * obar) / SQRT(v);
    IF 1 < anom_corr THEN RETURN 'NA'; END IF;
    RETURN IFNULL(anom_corr, 'NA');
END |


--
--  Vl1l2 stat calculations
--

DROP FUNCTION IF EXISTS calcVL1L2_FBAR |
CREATE FUNCTION calcVL1L2_FBAR(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL, uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        IF 0 = uvffbar THEN RETURN 'NA'; END IF;
        SET result = SQRT(uvffbar);
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_OBAR |

CREATE FUNCTION calcVL1L2_OBAR(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL, uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        IF 0 = uvffbar THEN RETURN 'NA'; END IF;
        SET result = SQRT(uvoobar);
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_ME |
CREATE FUNCTION calcVL1L2_ME(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL, uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result = SQRT(POW(ufbar, 2) - 2 * ufbar * uobar + POW(uobar, 2) + POW(vfbar, 2) - 2 * vfbar * vobar + POW(vobar, 2));
        RETURN IFNULL(result, 'NA');
    END |


DROP FUNCTION IF EXISTS calcVL1L2_BIAS |
CREATE FUNCTION calcVL1L2_BIAS(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result = SQRT(uvffbar) - SQRT(uvoobar);
        RETURN IFNULL(result, 'NA');
    END |


DROP FUNCTION IF EXISTS calcVL1L2_MSE |
CREATE FUNCTION calcVL1L2_MSE(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result = uvffbar -2 * uvfobar + uvoobar;
        if result < 0 THEN RETURN 'NA'; END IF;
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_RMSE |
CREATE FUNCTION calcVL1L2_RMSE(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result = SQRT( calcVL1L2_MSE(total , ufbar , vfbar , uobar , vobar , uvfobar , uvffbar ,uvoobar ) );
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_MAE |
CREATE FUNCTION calcVL1L2_MAE(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =  SQRT(POW(ufbar, 2) -2 * ufbar * uobar + POW(uobar, 2) + POW(vfbar, 2) - 2 * vfbar * vobar + POW(vobar, 2));
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_FVAR |
CREATE FUNCTION calcVL1L2_FVAR(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =  total * (uvffbar - POW(ufbar, 2) - POW(vfbar, 2)) / total;
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_OVAR |
CREATE FUNCTION calcVL1L2_OVAR(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =  total *( uvoobar - POW(uobar, 2) - POW(vobar, 2) )/ total;
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_FSTDEV |
CREATE FUNCTION calcVL1L2_FSTDEV(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =  SQRT( calcVL1L2_FVAR (total , ufbar , vfbar , uobar , vobar , uvfobar , uvffbar , uvoobar));
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_OSTDEV |
CREATE FUNCTION calcVL1L2_OSTDEV(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =  SQRT( calcVL1L2_OVAR (total , ufbar , vfbar , uobar , vobar , uvfobar , uvffbar , uvoobar));
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_FOSTDEV |
CREATE FUNCTION calcVL1L2_FOSTDEV(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =  SQRT( total * (uvffbar - POW(ufbar, 2) +uvoobar-POW(uobar, 2)-POW(vobar, 2) -2*(uvfobar-ufbar*uobar - vfbar*vobar))/total);
        RETURN IFNULL(result, 'NA');
    END |


DROP FUNCTION IF EXISTS calcVL1L2_COV |
CREATE FUNCTION calcVL1L2_COV(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =   total * (uvfobar -ufbar*uobar - vfbar * vobar) / (total-0.);
        RETURN IFNULL(result, 'NA');
    END |

DROP FUNCTION IF EXISTS calcVL1L2_CORR |
CREATE FUNCTION calcVL1L2_CORR(total INT, ufbar REAL, vfbar REAL, uobar REAL, vobar REAL, uvfobar REAL, uvffbar REAL,
    uvoobar REAL) RETURNS CHAR(16) DETERMINISTIC
    BEGIN
        DECLARE result DECIMAL(12, 6);
        SET result =   (total * (uvfobar - ufbar * uobar - vfbar * vobar) / total) /
                       (SQRT(total*(uvffbar-ufbar*ufbar-vfbar*vfbar)/total) *
            SQRT( total*(uvoobar-uobar*uobar-vobar*vobar)/total));
        RETURN IFNULL(result, 'NA');
    END |


DELIMITER ;


