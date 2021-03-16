-- data_file_type_lu is a look-up table containing information about the different types
--   of MET output data files.  Each data file that is loaded into the DATABASE is
--   represented by a record in the data_file table, which points at one of the data file
--   types.  The file type indicates which DATABASE tables store the data in the file.

DROP TABLE IF EXISTS data_file_lu;
CREATE TABLE data_file_lu
(
    data_file_lu_id INT UNSIGNED NOT NULL,
    type_name       VARCHAR(32),
    type_desc       VARCHAR(128),
    PRIMARY KEY (data_file_lu_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- data_file_id stores information about files that have been parsed and loaded into the
--   DATABASE.  Each record represents a single file of a particular MET output data file
--   type (point_stat, mode, etc.).  Each data_file record points at its file type in the
--   data_file_type_lu table via the data_file_type_lu_id field.

DROP TABLE IF EXISTS data_file;
CREATE TABLE data_file
(
    data_file_id    INT UNSIGNED NOT NULL,
    data_file_lu_id INT UNSIGNED NOT NULL,
    filename        VARCHAR(110),
    path            VARCHAR(120),
    load_date       DATETIME,
    mod_date        DATETIME,
    PRIMARY KEY (data_file_id),
    CONSTRAINT data_file_unique_pk
        UNIQUE INDEX (filename, path),
    CONSTRAINT stat_header_data_file_lu_id_pk
        FOREIGN KEY (data_file_lu_id)
            REFERENCES data_file_lu (data_file_lu_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- stat_header contains the forecast and observation bookkeeping information, except for
--   the valid and init times, for a verification case.  Statistics tables point at a
--   single stat_header record, which indicate the circumstances under which they were
--   calculated.

DROP TABLE IF EXISTS stat_header;
CREATE TABLE stat_header
(
    stat_header_id INT UNSIGNED NOT NULL,
    version        VARCHAR(8),
    model          VARCHAR(40),
    descr          VARCHAR(40)  DEFAULT 'NA',
    fcst_var       VARCHAR(50),
    fcst_units     VARCHAR(100) DEFAULT 'NA',
    fcst_lev       VARCHAR(100),
    obs_var        VARCHAR(50),
    obs_units      VARCHAR(100) DEFAULT 'NA',
    obs_lev        VARCHAR(100),
    obtype         VARCHAR(20),
    vx_mask        VARCHAR(100),
    interp_mthd    VARCHAR(20),
    interp_pnts    INT UNSIGNED,
    fcst_thresh    VARCHAR(100),
    obs_thresh     VARCHAR(100),

    PRIMARY KEY (stat_header_id)


) ENGINE = MyISAM
  CHARACTER SET = latin1;
CREATE INDEX stat_header_unique_pk ON stat_header (
                                                   model,
                                                   fcst_var(20),
                                                   fcst_lev (10),
                                                   obs_var(20),
                                                   obs_lev(10),
                                                   obtype(10),
                                                   vx_mask(20),
                                                   interp_mthd,
                                                   interp_pnts,
                                                   fcst_thresh(20),
                                                   obs_thresh(20)
    );

-- line_data_fho contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_fho;
CREATE TABLE line_data_fho
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    f_rate         DOUBLE,
    h_rate         DOUBLE,
    o_rate         DOUBLE,

    CONSTRAINT line_data_fho_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_fho_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_ctc contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_ctc;
CREATE TABLE line_data_ctc
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    fy_oy          INT UNSIGNED,
    fy_on          INT UNSIGNED,
    fn_oy          INT UNSIGNED,
    fn_on          INT UNSIGNED,

    CONSTRAINT line_data_ctc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_ctc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_cts contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_cts;
CREATE TABLE line_data_cts
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    alpha          DOUBLE,
    total          INT UNSIGNED,

    baser          DOUBLE,
    baser_ncl      DOUBLE,
    baser_ncu      DOUBLE,
    baser_bcl      DOUBLE,
    baser_bcu      DOUBLE,
    fmean          DOUBLE,
    fmean_ncl      DOUBLE,
    fmean_ncu      DOUBLE,
    fmean_bcl      DOUBLE,
    fmean_bcu      DOUBLE,
    acc            DOUBLE,
    acc_ncl        DOUBLE,
    acc_ncu        DOUBLE,
    acc_bcl        DOUBLE,
    acc_bcu        DOUBLE,
    fbias          DOUBLE,
    fbias_bcl      DOUBLE,
    fbias_bcu      DOUBLE,
    pody           DOUBLE,
    pody_ncl       DOUBLE,
    pody_ncu       DOUBLE,
    pody_bcl       DOUBLE,
    pody_bcu       DOUBLE,
    podn           DOUBLE,
    podn_ncl       DOUBLE,
    podn_ncu       DOUBLE,
    podn_bcl       DOUBLE,
    podn_bcu       DOUBLE,
    pofd           DOUBLE,
    pofd_ncl       DOUBLE,
    pofd_ncu       DOUBLE,
    pofd_bcl       DOUBLE,
    pofd_bcu       DOUBLE,
    far            DOUBLE,
    far_ncl        DOUBLE,
    far_ncu        DOUBLE,
    far_bcl        DOUBLE,
    far_bcu        DOUBLE,
    csi            DOUBLE,
    csi_ncl        DOUBLE,
    csi_ncu        DOUBLE,
    csi_bcl        DOUBLE,
    csi_bcu        DOUBLE,
    gss            DOUBLE,
    gss_bcl        DOUBLE,
    gss_bcu        DOUBLE,
    hk             DOUBLE,
    hk_ncl         DOUBLE,
    hk_ncu         DOUBLE,
    hk_bcl         DOUBLE,
    hk_bcu         DOUBLE,
    hss            DOUBLE,
    hss_bcl        DOUBLE,
    hss_bcu        DOUBLE,
    odds           DOUBLE,
    odds_ncl       DOUBLE,
    odds_ncu       DOUBLE,
    odds_bcl       DOUBLE,
    odds_bcu       DOUBLE,

    lodds          DOUBLE DEFAULT -9999,
    lodds_ncl      DOUBLE DEFAULT -9999,
    lodds_ncu      DOUBLE DEFAULT -9999,
    lodds_bcl      DOUBLE DEFAULT -9999,
    lodds_bcu      DOUBLE DEFAULT -9999,

    orss           DOUBLE DEFAULT -9999,
    orss_ncl       DOUBLE DEFAULT -9999,
    orss_ncu       DOUBLE DEFAULT -9999,
    orss_bcl       DOUBLE DEFAULT -9999,
    orss_bcu       DOUBLE DEFAULT -9999,

    eds            DOUBLE DEFAULT -9999,
    eds_ncl        DOUBLE DEFAULT -9999,
    eds_ncu        DOUBLE DEFAULT -9999,
    eds_bcl        DOUBLE DEFAULT -9999,
    eds_bcu        DOUBLE DEFAULT -9999,

    seds           DOUBLE DEFAULT -9999,
    seds_ncl       DOUBLE DEFAULT -9999,
    seds_ncu       DOUBLE DEFAULT -9999,
    seds_bcl       DOUBLE DEFAULT -9999,
    seds_bcu       DOUBLE DEFAULT -9999,

    edi            DOUBLE DEFAULT -9999,
    edi_ncl        DOUBLE DEFAULT -9999,
    edi_ncu        DOUBLE DEFAULT -9999,
    edi_bcl        DOUBLE DEFAULT -9999,
    edi_bcu        DOUBLE DEFAULT -9999,

    sedi           DOUBLE DEFAULT -9999,
    sedi_ncl       DOUBLE DEFAULT -9999,
    sedi_ncu       DOUBLE DEFAULT -9999,
    sedi_bcl       DOUBLE DEFAULT -9999,
    sedi_bcu       DOUBLE DEFAULT -9999,

    bagss          DOUBLE DEFAULT -9999,
    bagss_bcl      DOUBLE DEFAULT -9999,
    bagss_bcu      DOUBLE DEFAULT -9999,

    CONSTRAINT line_data_cts_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_cts_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;
-- CREATE INDEX line_data_cts_fcst_lead_pk ON line_data_cts (fcst_lead);
-- CREATE INDEX line_data_cts_fcst_valid_beg_pk ON line_data_cts (fcst_valid_beg);
-- CREATE INDEX line_data_cts_fcst_init_beg_pk ON line_data_cts (fcst_init_beg);


-- line_data_cnt contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_cnt;
CREATE TABLE line_data_cnt
(
    stat_header_id       INT UNSIGNED NOT NULL,
    data_file_id         INT UNSIGNED NOT NULL,
    line_num             INT UNSIGNED,
    fcst_lead            INT,
    fcst_valid_beg       DATETIME,
    fcst_valid_end       DATETIME,
    fcst_init_beg        DATETIME,
    obs_lead             INT UNSIGNED,
    obs_valid_beg        DATETIME,
    obs_valid_end        DATETIME,
    alpha                DOUBLE,
    total                INT UNSIGNED,
    fbar                 DOUBLE,
    fbar_ncl             DOUBLE,
    fbar_ncu             DOUBLE,
    fbar_bcl             DOUBLE,
    fbar_bcu             DOUBLE,
    fstdev               DOUBLE,
    fstdev_ncl           DOUBLE,
    fstdev_ncu           DOUBLE,
    fstdev_bcl           DOUBLE,
    fstdev_bcu           DOUBLE,
    obar                 DOUBLE,
    obar_ncl             DOUBLE,
    obar_ncu             DOUBLE,
    obar_bcl             DOUBLE,
    obar_bcu             DOUBLE,
    ostdev               DOUBLE,
    ostdev_ncl           DOUBLE,
    ostdev_ncu           DOUBLE,
    ostdev_bcl           DOUBLE,
    ostdev_bcu           DOUBLE,
    pr_corr              DOUBLE,
    pr_corr_ncl          DOUBLE,
    pr_corr_ncu          DOUBLE,
    pr_corr_bcl          DOUBLE,
    pr_corr_bcu          DOUBLE,
    sp_corr              DOUBLE,
    dt_corr              DOUBLE,
    ranks                INT UNSIGNED,
    frank_ties           INT,
    orank_ties           INT,
    me                   DOUBLE,
    me_ncl               DOUBLE,
    me_ncu               DOUBLE,
    me_bcl               DOUBLE,
    me_bcu               DOUBLE,
    estdev               DOUBLE,
    estdev_ncl           DOUBLE,
    estdev_ncu           DOUBLE,
    estdev_bcl           DOUBLE,
    estdev_bcu           DOUBLE,
    mbias                DOUBLE,
    mbias_bcl            DOUBLE,
    mbias_bcu            DOUBLE,
    mae                  DOUBLE,
    mae_bcl              DOUBLE,
    mae_bcu              DOUBLE,
    mse                  DOUBLE,
    mse_bcl              DOUBLE,
    mse_bcu              DOUBLE,
    bcmse                DOUBLE,
    bcmse_bcl            DOUBLE,
    bcmse_bcu            DOUBLE,
    rmse                 DOUBLE,
    rmse_bcl             DOUBLE,
    rmse_bcu             DOUBLE,
    e10                  DOUBLE,
    e10_bcl              DOUBLE,
    e10_bcu              DOUBLE,
    e25                  DOUBLE,
    e25_bcl              DOUBLE,
    e25_bcu              DOUBLE,
    e50                  DOUBLE,
    e50_bcl              DOUBLE,
    e50_bcu              DOUBLE,
    e75                  DOUBLE,
    e75_bcl              DOUBLE,
    e75_bcu              DOUBLE,
    e90                  DOUBLE,
    e90_bcl              DOUBLE,
    e90_bcu              DOUBLE,
    iqr                  DOUBLE DEFAULT -9999,
    iqr_bcl              DOUBLE DEFAULT -9999,
    iqr_bcu              DOUBLE DEFAULT -9999,
    mad                  DOUBLE DEFAULT -9999,
    mad_bcl              DOUBLE DEFAULT -9999,
    mad_bcu              DOUBLE DEFAULT -9999,
    anom_corr            DOUBLE DEFAULT -9999,
    anom_corr_ncl        DOUBLE DEFAULT -9999,
    anom_corr_ncu        DOUBLE DEFAULT -9999,
    anom_corr_bcl        DOUBLE DEFAULT -9999,
    anom_corr_bcu        DOUBLE DEFAULT -9999,

    me2                  DOUBLE DEFAULT -9999,
    me2_bcl              DOUBLE DEFAULT -9999,
    me2_bcu              DOUBLE DEFAULT -9999,
    msess                DOUBLE DEFAULT -9999,
    msess_bcl            DOUBLE DEFAULT -9999,
    msess_bcu            DOUBLE DEFAULT -9999,
    rmsfa                DOUBLE DEFAULT -9999,
    rmsfa_bcl            DOUBLE DEFAULT -9999,
    rmsfa_bcu            DOUBLE DEFAULT -9999,
    rmsoa                DOUBLE DEFAULT -9999,
    rmsoa_bcl            DOUBLE DEFAULT -9999,
    rmsoa_bcu            DOUBLE DEFAULT -9999,

    anom_corr_uncntr     DOUBLE DEFAULT -9999,
    anom_corr_uncntr_bcl DOUBLE DEFAULT -9999,
    anom_corr_uncntr_bcu DOUBLE DEFAULT -9999,


    CONSTRAINT line_data_cnt_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_cnt_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_ecnt contains stat data for a Continuous Ensemble Statistics.

DROP TABLE IF EXISTS line_data_ecnt;
CREATE TABLE line_data_ecnt
(
    stat_header_id   INT UNSIGNED NOT NULL,
    data_file_id     INT UNSIGNED NOT NULL,
    line_num         INT UNSIGNED,
    fcst_lead        INT,
    fcst_valid_beg   DATETIME,
    fcst_valid_end   DATETIME,
    fcst_init_beg    DATETIME,
    obs_lead         INT UNSIGNED,
    obs_valid_beg    DATETIME,
    obs_valid_end    DATETIME,

    total            INT UNSIGNED,
    n_ens            INT,
    crps             DOUBLE,
    crpss            DOUBLE,
    ign              DOUBLE,
    me               DOUBLE,
    rmse             DOUBLE,
    spread           DOUBLE,
    me_oerr          DOUBLE,
    rmse_oerr        DOUBLE,
    spread_oerr      DOUBLE,
    spread_plus_oerr DOUBLE,

    crpscl           DOUBLE,
    crps_emp         DOUBLE,
    crpscl_emp       DOUBLE,
    crpss_emp        DOUBLE,

    CONSTRAINT line_data_ecnt_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_ecnt_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_mctc contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mctc;
CREATE TABLE line_data_mctc
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,
    n_cat          INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_mctc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_mctc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_mctc_cnt contains count data for a particular line_data_mctc record.  The
--   number of counts is determined by assuming a square contingency table and stored in
--   the line_data_mctc field n_cat.

DROP TABLE IF EXISTS line_data_mctc_cnt;
CREATE TABLE line_data_mctc_cnt
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    j_value      INT UNSIGNED NOT NULL,
    fi_oj        INT UNSIGNED NOT NULL,

    PRIMARY KEY (line_data_id, i_value, j_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_mcts contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mcts;
CREATE TABLE line_data_mcts
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    alpha          DOUBLE,
    total          INT UNSIGNED,
    n_cat          INT UNSIGNED,

    acc            DOUBLE,
    acc_ncl        DOUBLE,
    acc_ncu        DOUBLE,
    acc_bcl        DOUBLE,
    acc_bcu        DOUBLE,
    hk             DOUBLE,
    hk_bcl         DOUBLE,
    hk_bcu         DOUBLE,
    hss            DOUBLE,
    hss_bcl        DOUBLE,
    hss_bcu        DOUBLE,
    ger            DOUBLE,
    ger_bcl        DOUBLE,
    ger_bcu        DOUBLE,

    CONSTRAINT line_data_mcts_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_mcts_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_pct contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_pct;
CREATE TABLE line_data_pct
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    cov_thresh     VARCHAR(32),
    total          INT UNSIGNED,
    n_thresh       INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pct_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_pct_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_pct_thresh contains threshold data for a particular line_data_pct record and
--   threshold.  The number of thresholds stored is given by the line_data_pct field n_thresh.

DROP TABLE IF EXISTS line_data_pct_thresh;
CREATE TABLE line_data_pct_thresh
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    thresh_i     DOUBLE,
    oy_i         INT UNSIGNED,
    on_i         INT UNSIGNED,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_pstd contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_pstd;
CREATE TABLE line_data_pstd
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    cov_thresh     VARCHAR(32),
    alpha          DOUBLE,
    total          INT UNSIGNED,
    n_thresh       INT UNSIGNED,

    baser          DOUBLE,
    baser_ncl      DOUBLE,
    baser_ncu      DOUBLE,
    reliability    DOUBLE,
    resolution     DOUBLE,
    uncertainty    DOUBLE,
    roc_auc        DOUBLE,
    brier          DOUBLE,
    brier_ncl      DOUBLE,
    brier_ncu      DOUBLE,

    briercl        DOUBLE DEFAULT -9999,
    briercl_ncl    DOUBLE DEFAULT -9999,
    briercl_ncu    DOUBLE DEFAULT -9999,
    bss            DOUBLE DEFAULT -9999,
    bss_smpl       DOUBLE DEFAULT -9999,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pstd_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_pstd_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_pstd_thresh contains threshold data for a particular line_data_pstd record and
--   threshold.  The number of thresholds stored is given by the line_data_pstd field n_thresh.

DROP TABLE IF EXISTS line_data_pstd_thresh;
CREATE TABLE line_data_pstd_thresh
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    thresh_i     DOUBLE,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_pjc contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_pjc;
CREATE TABLE line_data_pjc
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    cov_thresh     VARCHAR(32),
    total          INT UNSIGNED,
    n_thresh       INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pjc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_pjc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_pjc_thresh contains threshold data for a particular line_data_pjc record and
--   threshold.  The number of thresholds stored is given by the line_data_pjc field n_thresh.

DROP TABLE IF EXISTS line_data_pjc_thresh;
CREATE TABLE line_data_pjc_thresh
(
    line_data_id  INT UNSIGNED NOT NULL,
    i_value       INT UNSIGNED NOT NULL,
    thresh_i      DOUBLE,
    oy_tp_i       DOUBLE,
    on_tp_i       DOUBLE,
    calibration_i DOUBLE,
    refinement_i  DOUBLE,
    likelihood_i  DOUBLE,
    baser_i       DOUBLE,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_prc contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_prc;
CREATE TABLE line_data_prc
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    cov_thresh     VARCHAR(32),
    total          INT UNSIGNED,
    n_thresh       INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_prc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_prc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_prc_thresh contains threshold data for a particular line_data_prc record and
--   threshold.  The number of thresholds stored is given by the line_data_prc field n_thresh.

DROP TABLE IF EXISTS line_data_prc_thresh;
CREATE TABLE line_data_prc_thresh
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    thresh_i     DOUBLE,
    pody_i       DOUBLE,
    pofd_i       DOUBLE,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_sl1l2 contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_sl1l2;
CREATE TABLE line_data_sl1l2
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    fbar           DOUBLE,
    obar           DOUBLE,
    fobar          DOUBLE,
    ffbar          DOUBLE,
    oobar          DOUBLE,
    mae            DOUBLE DEFAULT -9999,

    CONSTRAINT line_data_sl1l2_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_sl1l2_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS line_data_grad;
CREATE TABLE line_data_grad
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    fgbar          DOUBLE,
    ogbar          DOUBLE,
    mgbar          DOUBLE,
    egbar          DOUBLE,
    s1             DOUBLE,
    s1_og          DOUBLE DEFAULT -9999,
    fgog_ratio     DOUBLE DEFAULT -9999,
    dx             INT    DEFAULT -9999,
    dy             INT    DEFAULT -9999,

    CONSTRAINT line_data_grad_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_grad_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_sal1l2 contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_sal1l2;
CREATE TABLE line_data_sal1l2
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    fabar          DOUBLE,
    oabar          DOUBLE,
    foabar         DOUBLE,
    ffabar         DOUBLE,
    ooabar         DOUBLE,
    mae            DOUBLE DEFAULT -9999,

    CONSTRAINT line_data_sal2l1_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_sal2l1_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_vl1l2 contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_vl1l2;
CREATE TABLE line_data_vl1l2
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    ufbar          DOUBLE,
    vfbar          DOUBLE,
    uobar          DOUBLE,
    vobar          DOUBLE,
    uvfobar        DOUBLE,
    uvffbar        DOUBLE,
    uvoobar        DOUBLE,
    f_speed_bar    DOUBLE,
    o_speed_bar    DOUBLE,

    CONSTRAINT line_data_vl1l2_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_vl1l2_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_val1l2 contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_val1l2;
CREATE TABLE line_data_val1l2
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    ufabar         DOUBLE,
    vfabar         DOUBLE,
    uoabar         DOUBLE,
    voabar         DOUBLE,
    uvfoabar       DOUBLE,
    uvffabar       DOUBLE,
    uvooabar       DOUBLE,

    CONSTRAINT line_data_val1l2_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_val1l2_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_mpr contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mpr;
CREATE TABLE line_data_mpr
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    mp_index       INT UNSIGNED,
    obs_sid        VARCHAR(32),
    obs_lat        DOUBLE,
    obs_lon        DOUBLE,
    obs_lvl        DOUBLE,
    obs_elv        DOUBLE,
    mpr_fcst       DOUBLE,
    mpr_obs        DOUBLE,
    mpr_climo      DOUBLE,
    obs_qc         VARCHAR(32),
    climo_mean     DOUBLE,
    climo_stdev    DOUBLE,
    climo_cdf      DOUBLE,

    CONSTRAINT line_data_mpr_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_mpr_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_nbrctc contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_nbrctc;
CREATE TABLE line_data_nbrctc
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    cov_thresh     VARCHAR(32),
    total          INT UNSIGNED,

    fy_oy          INT UNSIGNED,
    fy_on          INT UNSIGNED,
    fn_oy          INT UNSIGNED,
    fn_on          INT UNSIGNED,

    CONSTRAINT line_data_nbrctc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_nbrctc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_nbrcts contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_nbrcts;
CREATE TABLE line_data_nbrcts
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    cov_thresh     VARCHAR(32),
    alpha          DOUBLE,
    total          INT UNSIGNED,

    baser          DOUBLE,
    baser_ncl      DOUBLE,
    baser_ncu      DOUBLE,
    baser_bcl      DOUBLE,
    baser_bcu      DOUBLE,
    fmean          DOUBLE,
    fmean_ncl      DOUBLE,
    fmean_ncu      DOUBLE,
    fmean_bcl      DOUBLE,
    fmean_bcu      DOUBLE,
    acc            DOUBLE,
    acc_ncl        DOUBLE,
    acc_ncu        DOUBLE,
    acc_bcl        DOUBLE,
    acc_bcu        DOUBLE,
    fbias          DOUBLE,
    fbias_bcl      DOUBLE,
    fbias_bcu      DOUBLE,
    pody           DOUBLE,
    pody_ncl       DOUBLE,
    pody_ncu       DOUBLE,
    pody_bcl       DOUBLE,
    pody_bcu       DOUBLE,
    podn           DOUBLE,
    podn_ncl       DOUBLE,
    podn_ncu       DOUBLE,
    podn_bcl       DOUBLE,
    podn_bcu       DOUBLE,
    pofd           DOUBLE,
    pofd_ncl       DOUBLE,
    pofd_ncu       DOUBLE,
    pofd_bcl       DOUBLE,
    pofd_bcu       DOUBLE,
    far            DOUBLE,
    far_ncl        DOUBLE,
    far_ncu        DOUBLE,
    far_bcl        DOUBLE,
    far_bcu        DOUBLE,
    csi            DOUBLE,
    csi_ncl        DOUBLE,
    csi_ncu        DOUBLE,
    csi_bcl        DOUBLE,
    csi_bcu        DOUBLE,
    gss            DOUBLE,
    gss_bcl        DOUBLE,
    gss_bcu        DOUBLE,
    hk             DOUBLE,
    hk_ncl         DOUBLE,
    hk_ncu         DOUBLE,
    hk_bcl         DOUBLE,
    hk_bcu         DOUBLE,
    hss            DOUBLE,
    hss_bcl        DOUBLE,
    hss_bcu        DOUBLE,
    odds           DOUBLE,
    odds_ncl       DOUBLE,
    odds_ncu       DOUBLE,
    odds_bcl       DOUBLE,
    odds_bcu       DOUBLE,

    lodds          DOUBLE DEFAULT -9999,
    lodds_ncl      DOUBLE DEFAULT -9999,
    lodds_ncu      DOUBLE DEFAULT -9999,
    lodds_bcl      DOUBLE DEFAULT -9999,
    lodds_bcu      DOUBLE DEFAULT -9999,

    orss           DOUBLE DEFAULT -9999,
    orss_ncl       DOUBLE DEFAULT -9999,
    orss_ncu       DOUBLE DEFAULT -9999,
    orss_bcl       DOUBLE DEFAULT -9999,
    orss_bcu       DOUBLE DEFAULT -9999,

    eds            DOUBLE DEFAULT -9999,
    eds_ncl        DOUBLE DEFAULT -9999,
    eds_ncu        DOUBLE DEFAULT -9999,
    eds_bcl        DOUBLE DEFAULT -9999,
    eds_bcu        DOUBLE DEFAULT -9999,

    seds           DOUBLE DEFAULT -9999,
    seds_ncl       DOUBLE DEFAULT -9999,
    seds_ncu       DOUBLE DEFAULT -9999,
    seds_bcl       DOUBLE DEFAULT -9999,
    seds_bcu       DOUBLE DEFAULT -9999,

    edi            DOUBLE DEFAULT -9999,
    edi_ncl        DOUBLE DEFAULT -9999,
    edi_ncu        DOUBLE DEFAULT -9999,
    edi_bcl        DOUBLE DEFAULT -9999,
    edi_bcu        DOUBLE DEFAULT -9999,

    sedi           DOUBLE DEFAULT -9999,
    sedi_ncl       DOUBLE DEFAULT -9999,
    sedi_ncu       DOUBLE DEFAULT -9999,
    sedi_bcl       DOUBLE DEFAULT -9999,
    sedi_bcu       DOUBLE DEFAULT -9999,

    bagss          DOUBLE DEFAULT -9999,
    bagss_bcl      DOUBLE DEFAULT -9999,
    bagss_bcu      DOUBLE DEFAULT -9999,

    CONSTRAINT line_data_nbrcts_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_nbrcts_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_nbrcnt contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_nbrcnt;
CREATE TABLE line_data_nbrcnt
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    alpha          DOUBLE,
    total          INT UNSIGNED,

    fbs            DOUBLE,
    fbs_bcl        DOUBLE,
    fbs_bcu        DOUBLE,
    fss            DOUBLE,
    fss_bcl        DOUBLE,
    fss_bcu        DOUBLE,
    afss           DOUBLE DEFAULT -9999,
    afss_bcl       DOUBLE DEFAULT -9999,
    afss_bcu       DOUBLE DEFAULT -9999,
    ufss           DOUBLE DEFAULT -9999,
    ufss_bcl       DOUBLE DEFAULT -9999,
    ufss_bcu       DOUBLE DEFAULT -9999,
    f_rate         DOUBLE DEFAULT -9999,
    f_rate_bcl     DOUBLE DEFAULT -9999,
    f_rate_bcu     DOUBLE DEFAULT -9999,
    o_rate         DOUBLE DEFAULT -9999,
    o_rate_bcl     DOUBLE DEFAULT -9999,
    o_rate_bcu     DOUBLE DEFAULT -9999,


    CONSTRAINT line_data_nbrcnt_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_nbrcnt_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS line_data_enscnt;
CREATE TABLE line_data_enscnt
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT UNSIGNED,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,

    rpsf           DOUBLE DEFAULT -9999,
    rpsf_ncl       DOUBLE DEFAULT -9999,
    rpsf_ncu       DOUBLE DEFAULT -9999,
    rpsf_bcl       DOUBLE DEFAULT -9999,
    rpsf_bcu       DOUBLE DEFAULT -9999,

    rpscl          DOUBLE DEFAULT -9999,
    rpscl_ncl      DOUBLE DEFAULT -9999,
    rpscl_ncu      DOUBLE DEFAULT -9999,
    rpscl_bcl      DOUBLE DEFAULT -9999,
    rpscl_bcu      DOUBLE DEFAULT -9999,

    rpss           DOUBLE DEFAULT -9999,
    rpss_ncl       DOUBLE DEFAULT -9999,
    rpss_ncu       DOUBLE DEFAULT -9999,
    rpss_bcl       DOUBLE DEFAULT -9999,
    rpss_bcu       DOUBLE DEFAULT -9999,

    crpsf          DOUBLE DEFAULT -9999,
    crpsf_ncl      DOUBLE DEFAULT -9999,
    crpsf_ncu      DOUBLE DEFAULT -9999,
    crpsf_bcl      DOUBLE DEFAULT -9999,
    crpsf_bcu      DOUBLE DEFAULT -9999,

    crpscl         DOUBLE DEFAULT -9999,
    crpscl_ncl     DOUBLE DEFAULT -9999,
    crpscl_ncu     DOUBLE DEFAULT -9999,
    crpscl_bcl     DOUBLE DEFAULT -9999,
    crpscl_bcu     DOUBLE DEFAULT -9999,

    crpss          DOUBLE DEFAULT -9999,
    crpss_ncl      DOUBLE DEFAULT -9999,
    crpss_ncu      DOUBLE DEFAULT -9999,
    crpss_bcl      DOUBLE DEFAULT -9999,
    crpss_bcu      DOUBLE DEFAULT -9999,


    CONSTRAINT line_data_enscnt_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_enscnt_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

--  contains stat data for a particular stat_header record, which it points
--    at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_isc;
CREATE TABLE line_data_isc
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    tile_dim       DOUBLE,
    time_xll       DOUBLE,
    tile_yll       DOUBLE,
    nscale         DOUBLE,
    iscale         DOUBLE,
    mse            DOUBLE,
    isc            DOUBLE,
    fenergy2       DOUBLE,
    oenergy2       DOUBLE,
    baser          DOUBLE,
    fbias          DOUBLE,

    CONSTRAINT line_data_isc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_isc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_rhist contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_rhist;
CREATE TABLE line_data_rhist
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    n_rank         INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_rhist_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_rhist_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_rhist_rank contains rank data for a particular line_data_rhist record.  The
--   number of ranks stored is given by the line_data_rhist field n_rank.

DROP TABLE IF EXISTS line_data_rhist_rank;
CREATE TABLE line_data_rhist_rank
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    rank_i       INT UNSIGNED,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_relp contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_relp;
CREATE TABLE line_data_relp
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,
    n_ens          INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_relp_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_relp_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_relp_rank contains rank data for a particular line_data_relp record.  The
--   number of ranks stored is given by the line_data_relp field n_rank.

DROP TABLE IF EXISTS line_data_relp_ens;
CREATE TABLE line_data_relp_ens
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    ens_i        DOUBLE,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_eclv contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_eclv;
CREATE TABLE line_data_eclv
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,
    baser          DOUBLE,
    value_baser    DOUBLE,
    n_pnt          INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_eclv_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_eclv_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_relp_rank contains rank data for a particular line_data_eclv record.  The
--   number of ranks stored is given by the line_data_eclv field n_pnt.

DROP TABLE IF EXISTS line_data_eclv_pnt;
CREATE TABLE line_data_eclv_pnt
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    x_pnt_i      DOUBLE,
    y_pnt_i      DOUBLE,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_phist contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_phist;
CREATE TABLE line_data_phist
(
    line_data_id   INT UNSIGNED NOT NULL,
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    total          INT UNSIGNED,

    bin_size       DOUBLE,
    n_bin          INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_phist_data_file_id_pk FOREIGN KEY (data_file_id) REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_phist_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_phist_rank contains rank data for a particular line_data_phist record.  The
--   number of ranks stored is given by the line_data_phist field n_rank.

DROP TABLE IF EXISTS line_data_phist_bin;
CREATE TABLE line_data_phist_bin
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    bin_i        INT UNSIGNED,

    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_orank contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_orank;
CREATE TABLE line_data_orank
(
    line_data_id     INT UNSIGNED NOT NULL,
    stat_header_id   INT UNSIGNED NOT NULL,
    data_file_id     INT UNSIGNED NOT NULL,
    line_num         INT UNSIGNED,
    fcst_lead        INT,
    fcst_valid_beg   DATETIME,
    fcst_valid_end   DATETIME,
    fcst_init_beg    DATETIME,
    obs_lead         INT UNSIGNED,
    obs_valid_beg    DATETIME,
    obs_valid_end    DATETIME,
    total            INT UNSIGNED,

    orank_index      INT UNSIGNED,
    obs_sid          VARCHAR(64),
    obs_lat          VARCHAR(64),
    obs_lon          VARCHAR(64),
    obs_lvl          VARCHAR(64),
    obs_elv          VARCHAR(64),
    obs              DOUBLE,
    pit              DOUBLE,
    rank             INT,
    n_ens_vld        INT UNSIGNED,
    n_ens            INT UNSIGNED,
    obs_qc           VARCHAR(32),
    ens_mean         DOUBLE DEFAULT -9999,
    climo_mean       DOUBLE DEFAULT -9999,
    spread           DOUBLE DEFAULT -9999,
    ens_mean_oerr    DOUBLE DEFAULT -9999,
    spread_oerr      DOUBLE DEFAULT -9999,
    spread_plus_oerr DOUBLE DEFAULT -9999,

    climo_stdev      DOUBLE DEFAULT -9999,


    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_orank_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_orank_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_orank_ens contains ensemble data for a particular line_data_orank record.  The
--   number of ens values stored is given by the line_data_orank field n_ens.

DROP TABLE IF EXISTS line_data_orank_ens;
CREATE TABLE line_data_orank_ens
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    ens_i        DOUBLE,
    PRIMARY KEY (line_data_id, i_value)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- line_data_nbrcnt contains stat data for a particular stat_header record, which it points
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_ssvar;
CREATE TABLE line_data_ssvar
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    alpha          DOUBLE,
    total          INT UNSIGNED,

    n_bin          INT UNSIGNED,
    bin_i          INT UNSIGNED,
    bin_n          INT UNSIGNED,
    var_min        DOUBLE,
    var_max        DOUBLE,
    var_mean       DOUBLE,
    fbar           DOUBLE,
    obar           DOUBLE,
    fobar          DOUBLE,
    ffbar          DOUBLE,
    oobar          DOUBLE,

    fbar_ncl       DOUBLE,
    fbar_ncu       DOUBLE,
    fstdev         DOUBLE,
    fstdev_ncl     DOUBLE,
    fstdev_ncu     DOUBLE,
    obar_ncl       DOUBLE,
    obar_ncu       DOUBLE,
    ostdev         DOUBLE,
    ostdev_ncl     DOUBLE,
    ostdev_ncu     DOUBLE,
    pr_corr        DOUBLE,
    pr_corr_ncl    DOUBLE,
    pr_corr_ncu    DOUBLE,
    me             DOUBLE,
    me_ncl         DOUBLE,
    me_ncu         DOUBLE,
    estdev         DOUBLE,
    estdev_ncl     DOUBLE,
    estdev_ncu     DOUBLE,
    mbias          DOUBLE,
    mse            DOUBLE,
    bcmse          DOUBLE,
    rmse           DOUBLE,
    CONSTRAINT line_data_ssvar_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_ssvar_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;


DROP TABLE IF EXISTS line_data_vcnt;
CREATE TABLE line_data_vcnt
(
    stat_header_id   INT UNSIGNED NOT NULL,
    data_file_id     INT UNSIGNED NOT NULL,
    line_num         INT UNSIGNED,
    fcst_lead        INT,
    fcst_valid_beg   DATETIME,
    fcst_valid_end   DATETIME,
    fcst_init_beg    DATETIME,
    obs_lead         INT UNSIGNED,
    obs_valid_beg    DATETIME,
    obs_valid_end    DATETIME,
    alpha            DOUBLE,
    total            INT UNSIGNED,
    fbar             DOUBLE,
    fbar_bcl         DOUBLE,
    fbar_bcu         DOUBLE,
    obar             DOUBLE,
    obar_bcl         DOUBLE,
    obar_bcu         DOUBLE,
    fs_rms           DOUBLE,
    fs_rms_bcl       DOUBLE,
    fs_rms_bcu       DOUBLE,
    os_rms           DOUBLE,
    os_rms_bcl       DOUBLE,
    os_rms_bcu       DOUBLE,
    msve             DOUBLE,
    msve_bcl         DOUBLE,
    msve_bcu         DOUBLE,
    rmsve            DOUBLE,
    rmsve_bcl        DOUBLE,
    rmsve_bcu        DOUBLE,
    fstdev           DOUBLE,
    fstdev_bcl       DOUBLE,
    fstdev_bcu       DOUBLE,
    ostdev           DOUBLE,
    ostdev_bcl       DOUBLE,
    ostdev_bcu       DOUBLE,
    fdir             DOUBLE,
    fdir_bcl         DOUBLE,
    fdir_bcu         DOUBLE,
    odir             DOUBLE,
    odir_bcl         DOUBLE,
    odir_bcu         DOUBLE,
    fbar_speed       DOUBLE,
    fbar_speed_bcl   DOUBLE,
    fbar_speed_bcu   DOUBLE,
    obar_speed       DOUBLE,
    obar_speed_bcl   DOUBLE,
    obar_speed_bcu   DOUBLE,
    vdiff_speed      DOUBLE,
    vdiff_speed_bcl  DOUBLE,
    vdiff_speed_bcu  DOUBLE,
    vdiff_dir        DOUBLE,
    vdiff_dir_bcl    DOUBLE,
    vdiff_dir_bcu    DOUBLE,
    speed_err        DOUBLE,
    speed_err_bcl    DOUBLE,
    speed_err_bcu    DOUBLE,
    speed_abserr     DOUBLE,
    speed_abserr_bcl DOUBLE,
    speed_abserr_bcu DOUBLE,
    dir_err          DOUBLE,
    dir_err_bcl      DOUBLE,
    dir_err_bcu      DOUBLE,
    dir_abserr       DOUBLE,
    dir_abserr_bcl   DOUBLE,
    dir_abserr_bcu   DOUBLE,

    CONSTRAINT line_data_vcnt_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_vcnt_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

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
    mode_header_id  INT UNSIGNED NOT NULL,
    line_type_lu_id INT UNSIGNED NOT NULL,
    data_file_id    INT UNSIGNED NOT NULL,
    linenumber      INT UNSIGNED,
    version         VARCHAR(8),
    model           VARCHAR(40),
    n_valid         INT UNSIGNED,
    grid_res        INT UNSIGNED,
    descr           VARCHAR(40)  DEFAULT 'NA',
    fcst_lead       INT,
    fcst_valid      DATETIME,
    fcst_accum      INT UNSIGNED,
    fcst_init       DATETIME,
    obs_lead        INT UNSIGNED,
    obs_valid       DATETIME,
    obs_accum       INT UNSIGNED,
    fcst_rad        INT UNSIGNED,
    fcst_thr        VARCHAR(100),
    obs_rad         INT UNSIGNED,
    obs_thr         VARCHAR(100),
    fcst_var        VARCHAR(50),
    fcst_units      VARCHAR(100) DEFAULT 'NA',
    fcst_lev        VARCHAR(100),
    obs_var         VARCHAR(50),
    obs_units       VARCHAR(100) DEFAULT 'NA',
    obs_lev         VARCHAR(100),
    PRIMARY KEY (mode_header_id),

    CONSTRAINT mode_header_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id)

) ENGINE = MyISAM
  CHARACTER SET = latin1;
CREATE INDEX mode_header_unique_pk ON mode_header (
                                                   model,
                                                   fcst_lead,
                                                   fcst_valid,
                                                   fcst_accum,
                                                   obs_lead,
                                                   obs_valid,
                                                   obs_accum,
                                                   fcst_rad,
                                                   fcst_thr(20),
                                                   obs_rad,
                                                   obs_thr(20),
                                                   fcst_var(20),
                                                   fcst_lev(10),
                                                   obs_var(20),
                                                   obs_lev(10)
    );

-- mode_cts contains mode cts data for a particular mode_header record, which it points
--   at via the mode_header_id field.

DROP TABLE IF EXISTS mode_cts;
CREATE TABLE mode_cts
(
    mode_header_id INT UNSIGNED NOT NULL,
    field          VARCHAR(16),
    total          INT UNSIGNED,
    fy_oy          INT UNSIGNED,
    fy_on          INT UNSIGNED,
    fn_oy          INT UNSIGNED,
    fn_on          INT UNSIGNED,
    baser          DOUBLE,
    fmean          DOUBLE,
    acc            DOUBLE,
    fbias          DOUBLE,
    pody           DOUBLE,
    podn           DOUBLE,
    pofd           DOUBLE,
    far            DOUBLE,
    csi            DOUBLE,
    gss            DOUBLE,
    hk             DOUBLE,
    hss            DOUBLE,
    odds           DOUBLE,
    CONSTRAINT mode_cts_mode_header_id_pk
        FOREIGN KEY (mode_header_id)
            REFERENCES mode_header (mode_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- mode_obj_single contains mode object data for a particular mode_header record, which it
--   points at via the mode_header_id field.  This table stores information only about 
--   single mode objects.  Mode object pair information is stored in the mode_obj_pair 
--   table.

DROP TABLE IF EXISTS mode_obj_single;
CREATE TABLE mode_obj_single
(
    mode_obj_id    INT UNSIGNED NOT NULL,
    mode_header_id INT UNSIGNED NOT NULL,
    object_id      VARCHAR(128),
    object_cat     VARCHAR(128),
    centroid_x     DOUBLE,
    centroid_y     DOUBLE,
    centroid_lat   DOUBLE,
    centroid_lon   DOUBLE,
    axis_avg       DOUBLE,
    length         DOUBLE,
    width          DOUBLE,
    area           INT,
    area_thresh    INT,
    curvature      DOUBLE,
    curvature_x    DOUBLE,
    curvature_y    DOUBLE,
    complexity     DOUBLE,
    intensity_10   DOUBLE,
    intensity_25   DOUBLE,
    intensity_50   DOUBLE,
    intensity_75   DOUBLE,
    intensity_90   DOUBLE,
    intensity_nn   DOUBLE,
    intensity_sum  DOUBLE,
    fcst_flag      TINYINT(1),
    simple_flag    TINYINT(1),
    matched_flag   TINYINT(1),
    PRIMARY KEY (mode_obj_id),
    CONSTRAINT mode_obj_single_mode_header_id_pk
        FOREIGN KEY (mode_header_id)
            REFERENCES mode_header (mode_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

-- mode_obj_pair contains mode object data for a particular mode_header record, which it
--   points at via the mode_header_id field.  This table stores information only about pairs
--   of mode objects.  Each mode_obj_pair record points at two mode_obj_single records, one
--   corresponding to the observed object (via mode_obj_obs) and one corresponding to the 
--   forecast object (via mode_obj_fcst). 

DROP TABLE IF EXISTS mode_obj_pair;
CREATE TABLE mode_obj_pair
(
    mode_obj_obs_id            INT UNSIGNED NOT NULL,
    mode_obj_fcst_id           INT UNSIGNED NOT NULL,
    mode_header_id             INT UNSIGNED NOT NULL,
    object_id                  VARCHAR(128),
    object_cat                 VARCHAR(128),
    centroid_dist              DOUBLE,
    boundary_dist              DOUBLE,
    convex_hull_dist           DOUBLE,
    angle_diff                 DOUBLE,
    aspect_diff                DOUBLE,
    area_ratio                 DOUBLE,
    intersection_area          INT UNSIGNED,
    union_area                 INT UNSIGNED,
    symmetric_diff             INTEGER,
    intersection_over_area     DOUBLE,
    curvature_ratio            DOUBLE,
    complexity_ratio           DOUBLE,
    percentile_intensity_ratio DOUBLE,
    interest                   DOUBLE,
    simple_flag                TINYINT(1),
    matched_flag               TINYINT(1),
    CONSTRAINT mode_obj_pair_mode_header_id_pk
        FOREIGN KEY (mode_header_id)
            REFERENCES mode_header (mode_header_id),
    CONSTRAINT mode_obj_pair_mode_obj_obs_pk
        FOREIGN KEY (mode_obj_obs_id)
            REFERENCES mode_obj_single (mode_obj_id),
    CONSTRAINT mode_obj_pair_mode_obj_fcst_pk
        FOREIGN KEY (mode_obj_fcst_id)
            REFERENCES mode_obj_single (mode_obj_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

--  look-up table data

INSERT INTO data_file_lu
VALUES (0, 'point_stat', 'Verification statistics for forecasts at observation points');
INSERT INTO data_file_lu
VALUES (1, 'grid_stat', 'Verification statistics for a matched forecast and observation grid');
INSERT INTO data_file_lu
VALUES (2, 'mode_cts', 'Contingency table counts and statistics comparing forecast and observations');
INSERT INTO data_file_lu
VALUES (3, 'mode_obj', 'Attributes for simple objects, merged cluster objects and pairs of objects');
INSERT INTO data_file_lu
VALUES (4, 'wavelet_stat',
        'Verification statistics for intensity-scale comparison of forecast and observations');
INSERT INTO data_file_lu
VALUES (5, 'ensemble_stat', 'Ensemble verification statistics');
INSERT INTO data_file_lu
VALUES (6, 'vsdb_point_stat',
        'Verification statistics for forecasts at observation points for vsdb files');
INSERT INTO data_file_lu
VALUES (7, 'stat', 'All verification statistics');
INSERT INTO data_file_lu
VALUES (8, 'mtd_2d', '2D spatial attributes for single simple objects
for each time index of their existence');
INSERT INTO data_file_lu
VALUES (9, 'mtd_3d_pc', 'Pair attributes for 3D composite objects');
INSERT INTO data_file_lu
VALUES (10, 'mtd_3d_ps', 'Pair attributes for 3D simple objects');
INSERT INTO data_file_lu
VALUES (11, 'mtd_3d_sc', 'Single attributes for 3D composite objects');
INSERT INTO data_file_lu
VALUES (12, 'mtd_3d_ss', 'Single attributes for 3D simple objects');
INSERT INTO data_file_lu
VALUES (13, 'tcst', 'PTC-Stat)');


-- instance_info contains information about the particular instance of metvdb, including
--   dates of data updates and information about data table contents
DROP TABLE IF EXISTS instance_info;
CREATE TABLE IF NOT EXISTS instance_info
(
    instance_info_id INT UNSIGNED NOT NULL,
    updater          VARCHAR(64),
    update_date      DATETIME,
    update_detail    VARCHAR(2048),
    load_xml         TEXT,
    PRIMARY KEY (instance_info_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS model_fcst_lead_offset;
CREATE TABLE model_fcst_lead_offset
(
    model            VARCHAR(64),
    fcst_lead_offset INT,
    PRIMARY KEY (model)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS mtd_header;
CREATE TABLE mtd_header
(
    mtd_header_id   INT UNSIGNED NOT NULL,
    line_type_lu_id INT UNSIGNED NOT NULL,
    data_file_id    INT UNSIGNED NOT NULL,
    revision_id     INT UNSIGNED,
    linenumber      INT UNSIGNED,
    version         VARCHAR(8),
    model           VARCHAR(40),
    descr           VARCHAR(40)  DEFAULT 'NA',
    fcst_lead       INT,
    fcst_valid      DATETIME,
    fcst_init       DATETIME,
    obs_lead        INT,
    obs_valid       DATETIME,
    t_delta         INT(11),
    fcst_t_beg      INT,
    fcst_t_end      INT,
    fcst_rad        INT UNSIGNED,
    fcst_thr        VARCHAR(100),
    obs_t_beg       INT,
    obs_t_end       INT,
    obs_rad         INT UNSIGNED,
    obs_thr         VARCHAR(100),
    fcst_var        VARCHAR(50),
    fcst_units      VARCHAR(100) DEFAULT 'NA',
    fcst_lev        VARCHAR(100),
    obs_var         VARCHAR(50),
    obs_units       VARCHAR(100) DEFAULT 'NA',
    obs_lev         VARCHAR(100),
    PRIMARY KEY (mtd_header_id),

    CONSTRAINT mtd_header_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;
CREATE INDEX mtd_header_unique_pk ON mtd_header (
                                                 model,
                                                 descr,
                                                 fcst_lead,
                                                 fcst_valid,
                                                 obs_lead,
                                                 obs_valid,
                                                 t_delta,
                                                 fcst_rad,
                                                 fcst_thr(20),
                                                 obs_rad,
                                                 obs_thr(20),
                                                 fcst_var(20),
                                                 fcst_lev(10),
                                                 obs_var(20),
                                                 obs_lev(10)
    );

DROP TABLE IF EXISTS mtd_2d_obj;
CREATE TABLE mtd_2d_obj
(
    mtd_header_id INT UNSIGNED NOT NULL,
    object_id     VARCHAR(128),
    object_cat    VARCHAR(128),
    time_index    DOUBLE,
    area          DOUBLE,
    centroid_x    DOUBLE,
    centroid_y    DOUBLE,
    centroid_lat  DOUBLE,
    centroid_lon  DOUBLE,
    axis_ang      DOUBLE,
    intensity_10  DOUBLE,
    intensity_25  DOUBLE,
    intensity_50  DOUBLE,
    intensity_75  DOUBLE,
    intensity_90  DOUBLE,
    intensity_nn  DOUBLE,
    fcst_flag     TINYINT(1)   NOT NULL,
    simple_flag   TINYINT(1)   NOT NULL,
    matched_flag  TINYINT(1)   NOT NULL,
    CONSTRAINT mtd_2d_obj_mtd_header_id_pk
        FOREIGN KEY (mtd_header_id)
            REFERENCES mtd_header (mtd_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS mtd_3d_obj_single;
CREATE TABLE mtd_3d_obj_single
(
    mtd_header_id   INT UNSIGNED NOT NULL,
    object_id       VARCHAR(128),
    object_cat      VARCHAR(128),
    centroid_x      DOUBLE,
    centroid_y      DOUBLE,
    centroid_t      DOUBLE,
    centroid_lat    DOUBLE,
    centroid_lon    DOUBLE,
    x_dot           DOUBLE,
    y_dot           DOUBLE,
    axis_ang        DOUBLE,
    volume          DOUBLE,
    start_time      DOUBLE,
    end_time        DOUBLE,
    cdist_travelled DOUBLE,
    intensity_10    DOUBLE,
    intensity_25    DOUBLE,
    intensity_50    DOUBLE,
    intensity_75    DOUBLE,
    intensity_90    DOUBLE,
    intensity_nn    DOUBLE,
    fcst_flag       TINYINT(1)   NOT NULL,
    simple_flag     TINYINT(1)   NOT NULL,
    matched_flag    TINYINT(1)   NOT NULL,
    CONSTRAINT mtd_3d_obj_single_mtd_header_id_pk
        FOREIGN KEY (mtd_header_id)
            REFERENCES mtd_header (mtd_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS mtd_3d_obj_pair;
CREATE TABLE mtd_3d_obj_pair
(
    mtd_header_id       INT UNSIGNED NOT NULL,
    object_id           VARCHAR(128),
    object_cat          VARCHAR(128),
    space_centroid_dist DOUBLE,
    time_centroid_delta DOUBLE,
    axis_diff           DOUBLE,
    speed_delta         DOUBLE,
    direction_diff      DOUBLE,
    volume_ratio        DOUBLE,
    start_time_delta    DOUBLE,
    end_time_delta      DOUBLE,
    intersection_volume DOUBLE,
    duration_diff       DOUBLE,
    interest            DOUBLE,
    simple_flag         TINYINT(1)   NOT NULL,
    matched_flag        TINYINT(1)   NOT NULL,
    CONSTRAINT mtd_3d_obj_pair_mtd_header_id_pk
        FOREIGN KEY (mtd_header_id)
            REFERENCES mtd_header (mtd_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;


DROP TABLE IF EXISTS metadata;
CREATE TABLE metadata
(
    category    VARCHAR(30)  NOT NULL DEFAULT '',
    description VARCHAR(300) NOT NULL DEFAULT ''

) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS line_data_perc;
CREATE TABLE line_data_perc
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    fcst_perc      DOUBLE,
    obs_perc       DOUBLE,


    CONSTRAINT line_data_perc_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_perc_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS line_data_dmap;
CREATE TABLE line_data_dmap
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    alpha          DOUBLE,
    total          INT UNSIGNED,
    fy             INT,
    oy             INT,
    fbias          DOUBLE,
    baddeley       DOUBLE,
    hausdorff      DOUBLE,
    med_fo         DOUBLE,
    med_of         DOUBLE,
    med_min        DOUBLE,
    med_max        DOUBLE,
    med_mean       DOUBLE,
    fom_fo         DOUBLE,
    fom_of         DOUBLE,
    fom_min        DOUBLE,
    fom_max        DOUBLE,
    fom_mean       DOUBLE,
    zhu_fo         DOUBLE,
    zhu_of         DOUBLE,
    zhu_min        DOUBLE,
    zhu_max        DOUBLE,
    zhu_mean       DOUBLE,
    CONSTRAINT line_data_dmap_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_dmap_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;


DROP TABLE IF EXISTS line_data_rps;
CREATE TABLE line_data_rps
(
    stat_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED,
    fcst_lead      INT,
    fcst_valid_beg DATETIME,
    fcst_valid_end DATETIME,
    fcst_init_beg  DATETIME,
    obs_lead       INT UNSIGNED,
    obs_valid_beg  DATETIME,
    obs_valid_end  DATETIME,
    alpha          DOUBLE,
    total          INT UNSIGNED,
    n_prob         INT,
    rps_rel        DOUBLE,
    rps_res        DOUBLE,
    rps_unc        DOUBLE,
    rps            DOUBLE,
    rpss           DOUBLE,
    rpss_smpl      DOUBLE,
    rps_comp       DOUBLE,

    CONSTRAINT line_data_rps_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_rps_stat_header_id_pk
        FOREIGN KEY (stat_header_id)
            REFERENCES stat_header (stat_header_id),
    INDEX stat_header_id_idx (stat_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;


DROP TABLE IF EXISTS tcst_header;
CREATE TABLE tcst_header
(
    tcst_header_id INT UNSIGNED NOT NULL,
    version        VARCHAR(8),
    amodel         VARCHAR(40),
    bmodel         VARCHAR(40),
    descr          VARCHAR(40) DEFAULT 'NA',
    storm_id       VARCHAR(40),
    basin          VARCHAR(40),
    cyclone        INT UNSIGNED,
    storm_name     VARCHAR(40),
    init_mask      VARCHAR(100),
    valid_mask     VARCHAR(100),
    PRIMARY KEY (tcst_header_id)


) ENGINE = MyISAM
  CHARACTER SET = latin1;
CREATE INDEX tcst_header_unique_pk ON tcst_header (
                                                   amodel,
                                                   bmodel,
                                                   storm_id,
                                                   basin,
                                                   cyclone,
                                                   storm_name,
                                                   init_mask,
                                                   valid_mask
    );

DROP TABLE IF EXISTS line_data_probrirw;
CREATE TABLE line_data_probrirw
(
    line_data_id   INT UNSIGNED NOT NULL,
    tcst_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_num       INT UNSIGNED NOT NULL,
    fcst_lead      INT,
    fcst_valid     DATETIME,
    fcst_init      DATETIME,
    alat           DOUBLE,
    alon           DOUBLE,
    blat           DOUBLE,
    blon           DOUBLE,
    initials       VARCHAR(10),
    tk_err         DOUBLE,
    x_err          DOUBLE,
    y_err          DOUBLE,
    adland         DOUBLE,
    bdland         DOUBLE,
    rirw_beg       INT,
    rirw_end       INT,
    rirw_window    INT,
    awind_end      INT,
    bwind_beg      INT,
    bwind_end      INT,
    bdelta         INT,
    bdelta_max     INT,
    blevel_beg     VARCHAR(2),
    blevel_end     VARCHAR(2),
    n_thresh       INT UNSIGNED,

    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_probrirw_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_probrirw_stat_header_id_pk
        FOREIGN KEY (tcst_header_id)
            REFERENCES tcst_header (tcst_header_id),
    INDEX tcst_header_id_idx (tcst_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;

DROP TABLE IF EXISTS line_data_probrirw_thresh;
CREATE TABLE line_data_probrirw_thresh
(
    line_data_id INT UNSIGNED NOT NULL,
    i_value      INT UNSIGNED NOT NULL,
    thresh_i     INT,
    prob_i       INT,

    PRIMARY KEY (line_data_id, i_value),
    CONSTRAINT line_data_probrirw_id_pk
        FOREIGN KEY (line_data_id)
            REFERENCES line_data_probrirw (line_data_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;


DROP TABLE IF EXISTS line_data_tcmpr;
CREATE TABLE line_data_tcmpr
(
    tcst_header_id INT UNSIGNED NOT NULL,
    data_file_id   INT UNSIGNED NOT NULL,
    line_number    INT UNSIGNED NOT NULL,
    fcst_lead      INT,
    fcst_valid     DATETIME,
    fcst_init      DATETIME,
    total          INT UNSIGNED,
    index_pair     INT UNSIGNED,
    level          VARCHAR(2),
    watch_warn     VARCHAR(2),
    initials       VARCHAR(10),
    alat           DOUBLE,
    alon           DOUBLE,
    blat           DOUBLE,
    blon           DOUBLE,
    tk_err         DOUBLE,
    x_err          DOUBLE,
    y_err          DOUBLE,
    altk_err       DOUBLE,
    crtk_err       DOUBLE,
    adland         DOUBLE,
    bdland         DOUBLE,
    amslp          DOUBLE,
    bmslp          DOUBLE,
    amax_wind      DOUBLE,
    bmax_wind      DOUBLE,

    aal_wind_34    DOUBLE,
    bal_wind_34    DOUBLE,
    ane_wind_34    DOUBLE,
    bne_wind_34    DOUBLE,
    ase_wind_34    DOUBLE,
    bse_wind_34    DOUBLE,
    asw_wind_34    DOUBLE,
    bsw_wind_34    DOUBLE,
    anw_wind_34    DOUBLE,
    bnw_wind_34    DOUBLE,

    aal_wind_50    DOUBLE,
    bal_wind_50    DOUBLE,
    ane_wind_50    DOUBLE,
    bne_wind_50    DOUBLE,
    ase_wind_50    DOUBLE,
    bse_wind_50    DOUBLE,
    asw_wind_50    DOUBLE,
    bsw_wind_50    DOUBLE,
    anw_wind_50    DOUBLE,
    bnw_wind_50    DOUBLE,

    aal_wind_64    DOUBLE,
    bal_wind_64    DOUBLE,
    ane_wind_64    DOUBLE,
    bne_wind_64    DOUBLE,
    ase_wind_64    DOUBLE,
    bse_wind_64    DOUBLE,
    asw_wind_64    DOUBLE,
    bsw_wind_64    DOUBLE,
    anw_wind_64    DOUBLE,
    bnw_wind_64    DOUBLE,

    aradp          DOUBLE,
    bradp          DOUBLE,
    arrp           DOUBLE,
    brrp           DOUBLE,
    amrd           DOUBLE,
    bmrd           DOUBLE,
    agusts         DOUBLE,
    bgusts         DOUBLE,
    aeye           DOUBLE,
    beye           DOUBLE,
    adir           DOUBLE,
    bdir           DOUBLE,
    aspeed         DOUBLE,
    bspeed         DOUBLE,
    adepth         VARCHAR(1),
    bdepth         VARCHAR(1),

    CONSTRAINT line_data_tcmpr_data_file_id_pk
        FOREIGN KEY (data_file_id)
            REFERENCES data_file (data_file_id),
    CONSTRAINT line_data_tcmpr_stat_header_id_pk
        FOREIGN KEY (tcst_header_id)
            REFERENCES tcst_header (tcst_header_id),
    INDEX tcst_header_id_idx (tcst_header_id)
) ENGINE = MyISAM
  CHARACTER SET = latin1;