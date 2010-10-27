-- This file is intended for MySQL

use mysql;
DROP DATABASE IF EXISTS metvdb_met_ncep;
CREATE DATABASE metvdb_met_ncep;
use metvdb_met_ncep;


-- data_file_type_lu is a look-up table containing information about the different types
--   of MET output data files.  Each data file that is loaded into the database is
--   represented by a record in the data_file table, which points at one of the data file
--   types.  The file type indicates which database tables store the data in the file.

CREATE TABLE data_file_lu
(
    data_file_lu_id     INT UNSIGNED NOT NULL,
    type_name           VARCHAR(32),
    type_desc           VARCHAR(128),
    PRIMARY KEY (data_file_lu_id)
);
    
    
-- data_file_id stores information about files that have been parsed and loaded into the
--   database.  Each record represents a single file of a particular MET output data file
--   type (point_stat, mode, etc.).  Each data_file record points at its file type in the
--   data_file_type_lu table via the data_file_type_lu_id field.

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
        UNIQUE INDEX (
            filename,
            path
        ),
    CONSTRAINT stat_header_data_file_lu_id_pk
            FOREIGN KEY(data_file_lu_id)
            REFERENCES data_file_lu(data_file_lu_id)
);


-- line_type_lu is a look-up table containing information about line types in a stat file, 
--   including the line type name which appears in field 21 of a stat file.  The description 
--   of the line type from the user manual is also included.
--   e.g. 'FHO', 'Forecast, Hit, Observation line type'

CREATE TABLE line_type_lu
(
    line_type_lu_id     INT UNSIGNED NOT NULL,
    line_type_name      VARCHAR(32),
    line_type_desc      VARCHAR(512),
    PRIMARY KEY (line_type_lu_id)
);


-- stat_header represents a line in a stat file and contains the header information for
--   that line.  The line-dependent information is stored in specific tables for each line 
--   type, each of which point at the line they are associated with, via the stat_header_id 
--   field.  Each stat_header line also specifies what type it is by pointing at a line
--   type in the line_type_lu table, via the line_type_lu_id field.

CREATE TABLE stat_header
(
    stat_header_id      INT UNSIGNED NOT NULL,
    version             VARCHAR(8),
    model               VARCHAR(64),
    fcst_lead           INT UNSIGNED,
    fcst_valid_beg      DATETIME,
    fcst_valid_end      DATETIME,
    fcst_init_beg       DATETIME,
    obs_lead            INT UNSIGNED,
    obs_valid_beg       DATETIME,
    obs_valid_end       DATETIME,
    fcst_var            VARCHAR(64),
    fcst_lev            VARCHAR(16),
    obs_var             VARCHAR(64),
    obs_lev             VARCHAR(16),
    obtype              VARCHAR(32),
    vx_mask             VARCHAR(32),
    interp_mthd         VARCHAR(16),
    interp_pnts         INT UNSIGNED,
    fcst_thresh         VARCHAR(16),
    obs_thresh          VARCHAR(16),
    PRIMARY KEY (stat_header_id),
    CONSTRAINT stat_header_unique_pk
        UNIQUE INDEX (
            model,
            fcst_lead,
            fcst_valid_beg,
            fcst_init_beg,
            obs_lead,
            obs_valid_beg,
            fcst_var,
            fcst_lev,
            obs_var,
            obs_lev,
            obtype,
            vx_mask,
            interp_mthd,
            interp_pnts,
            fcst_thresh,
            obs_thresh
        )
);


-- stat_group_lu is a look-up table containing type information about a type of stat_group.  
--   Each record is pointed at by all the stat_groups whose type it describes.  Each type
--   of stat group is associated with a line type and each stat group is associated with
--   a line.  The table also contains description information from the user's guide.  The 
--   presence of the confidence interval data is given by the corresponding BOOLEANs.  

CREATE TABLE stat_group_lu
(
    stat_group_lu_id    INT UNSIGNED NOT NULL,
    stat_group_name     VARCHAR(16),
    stat_group_desc     VARCHAR(512),
    stat_nc_used        BOOLEAN,
    stat_bc_used        BOOLEAN,
    line_type_lu_id     INT UNSIGNED NOT NULL,
    PRIMARY KEY (stat_group_lu_id),
    CONSTRAINT stat_group_lu_line_type_lu_pk
            FOREIGN KEY(line_type_lu_id)
            REFERENCES line_type_lu(line_type_lu_id)
);


-- stat_group contains a single group of statistics for a particular line.  A stat file 
--   line may have one or more stat groups.  Each stat group has a quantity (mean in some 
--   cases) and one or two confidence intervals, normal and/or bootstrap.  The stat group 
--   points at its stat group type via the stat_group_lu_id field, and points at its stat 
--   line via the stat_header_id field.  Note that the stat group *does not* point at its 
--   corresponding line group, which contains the rest of the line specific information.

CREATE TABLE stat_group
(
    stat_group_lu_id    INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    line_data_id        INT UNSIGNED NOT NULL,
    stat_value          DOUBLE,
    stat_ncl            DOUBLE,
    stat_ncu            DOUBLE,
    stat_bcl            DOUBLE,
    stat_bcu            DOUBLE,
    CONSTRAINT stat_group_stat_group_lu_pk
            FOREIGN KEY(stat_group_lu_id)
            REFERENCES stat_group_lu(stat_group_lu_id),
    CONSTRAINT stat_group_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);
    

-- line_data_fho contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_fho
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    f_rate              DOUBLE,
    h_rate              DOUBLE,
    o_rate              DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_fho_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_ctc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_ctc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    fy_oy               DOUBLE,
    fy_on               DOUBLE,
    fn_oy               DOUBLE,
    fn_on               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_ctc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_cts contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_cts
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    alpha               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_cts_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_cnt contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_cnt
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    alpha               DOUBLE,    
    sp_corr             DOUBLE,
    kt_corr             DOUBLE,
    ranks               DOUBLE,
    frank_ties          DOUBLE,
    orank_ties          DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_cnt_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_pct contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_pct
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pct_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_pct_thresh contains threshold data for a particular line_data_pct record and
--   threshold.  The number of thresholds stored is given by the line_data_pct field n_thresh.

CREATE TABLE line_data_pct_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    oy_i                INT UNSIGNED,
    on_i                INT UNSIGNED,
    CONSTRAINT line_data_pct_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_pct(line_data_id)
);


-- line_data_pstd contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_pstd
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    alpha               DOUBLE,
    n_thresh            INT UNSIGNED,
   PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pstd_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_pstd_thresh contains threshold data for a particular line_data_pstd record and
--   threshold.  The number of thresholds stored is given by the line_data_pstd field n_thresh.

CREATE TABLE line_data_pstd_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    CONSTRAINT line_data_pstd_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_pstd(line_data_id)
);


-- line_data_pjc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_pjc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_pjc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_pjc_thresh contains threshold data for a particular line_data_pjc record and
--   threshold.  The number of thresholds stored is given by the line_data_pjc field n_thresh.

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
    CONSTRAINT line_data_pjc_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_pjc(line_data_id)
);


-- line_data_prc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_prc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    n_thresh            INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_prc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_prc_thresh contains threshold data for a particular line_data_prc record and
--   threshold.  The number of thresholds stored is given by the line_data_prc field n_thresh.

CREATE TABLE line_data_prc_thresh
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    thresh_i            DOUBLE,
    pody_i              DOUBLE,
    pofd_i              DOUBLE,
    CONSTRAINT line_data_prc_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_prc(line_data_id)
);


-- line_data_sl1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_sl1l2
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    fbar                DOUBLE,
    obar                DOUBLE,
    fobar               DOUBLE,
    ffbar               DOUBLE,
    oobar               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_sl1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_sal1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_sal1l2
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    fabar               DOUBLE,
    oabar               DOUBLE,
    foabar              DOUBLE,
    ffabar              DOUBLE,
    ooabar              DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_sal1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_vl1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_vl1l2
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    ufbar               DOUBLE,
    vfbar               DOUBLE,
    uobar               DOUBLE,
    vobar               DOUBLE,
    uvfobar             DOUBLE,
    uvffbar             DOUBLE,
    uvoobar             DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_vl1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_val1l2 contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_val1l2
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    ufabar              DOUBLE,
    vfabar              DOUBLE,
    uoabar              DOUBLE,
    voabar              DOUBLE,
    uvfoabar            DOUBLE,
    uvffabar            DOUBLE,
    uvooabar            DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_val1l2_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_mpr contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_mpr
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    mp_index            DOUBLE,
    obs_lat             DOUBLE,
    obs_lon             DOUBLE,
    obs_lvl             DOUBLE,
    obs_elv             DOUBLE,
    fcst                DOUBLE,
    obs                 DOUBLE,
    climo               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_mpr_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_nbrctc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_nbrctc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    cov_thresh          VARCHAR(32),
    fy_oy               INT UNSIGNED,
    fy_on               INT UNSIGNED,
    fn_oy               INT UNSIGNED,
    fn_on               INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_nbrctc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_nbrcts contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_nbrcts
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    cov_thresh          VARCHAR(32),
    alpha               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_nbrcts_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_nbrcnt contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_nbrcnt
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    alpha               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_nbrcnt_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_isc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_isc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    tile_dim            DOUBLE,
    time_xll            DOUBLE,
    tile_yll            DOUBLE,
    nscale              DOUBLE,
    iscale              DOUBLE,
    mse                 DOUBLE,
    fenergy2            DOUBLE,
    oenergy2            DOUBLE,
    baser               DOUBLE,
    fbias               DOUBLE,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_isc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_rhist contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_rhist
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    crps                DOUBLE,
    ign                 DOUBLE,
    n_rank              INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_rhist_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_rhist_rank contains rank data for a particular line_data_rhist record.  The 
--   number of ranks stored is given by the line_data_rhist field n_rank.

CREATE TABLE line_data_rhist_rank
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    rank_i              INT UNSIGNED,
    CONSTRAINT line_data_rhist_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_rhist(line_data_id)
);


-- line_data_orank contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

CREATE TABLE line_data_orank
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    orank_index         INT UNSIGNED,
    obs_sid             VARCHAR(64),
    obs_lat             VARCHAR(64),
    obs_lon             VARCHAR(64),
    obs_lvl             VARCHAR(64),
    obs_elv             VARCHAR(64),
    obs                 DOUBLE,
    pit                 DOUBLE,
    rank                INT UNSIGNED,
    n_ens_vld           INT UNSIGNED,
    n_ens               INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_orank_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_orank_ens contains ensemble data for a particular line_data_orank record.  The 
--   number of ens values stored is given by the line_data_orank field n_ens.

CREATE TABLE line_data_orank_ens
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    ens_i               DOUBLE,
    CONSTRAINT line_data_orank_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_orank(line_data_id)
);


-- mode_header represents a line in a mode file and contains the header information for
--   that line.  The line-dependent information is stored in specific tables for each line 
--   type, each of which point at the line they are associated with, via the mode_header_id 
--   field.  Each mode_header line also specifies what type it is by pointing at a line
--   type in the line_type_lu table, via the line_type_lu_id field.  The file that the
--   line information was stored in is specified by a record in the data_file table, pointed
--   at by the data_file_id field.

CREATE TABLE mode_header
(
    mode_header_id      INT UNSIGNED NOT NULL,
    line_type_lu_id     INT UNSIGNED NOT NULL,
    data_file_id        INT UNSIGNED NOT NULL,
    linenumber          INT UNSIGNED,
    version             VARCHAR(8),
    model               VARCHAR(64),
    fcst_lead           INT UNSIGNED,
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
    CONSTRAINT mode_header_line_type_lu_pk
        FOREIGN KEY(line_type_lu_id)
        REFERENCES line_type_lu(line_type_lu_id),
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
);


-- mode_cts contains mode cts data for a particular mode_header record, which it points 
--   at via the mode_header_id field.

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
);


-- mode_obj_single contains mode object data for a particular mode_header record, which it 
--   points at via the mode_header_id field.  This table stores information only about 
--   single mode objects.  Mode object pair information is stored in the mode_obj_pair 
--   table.

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
);


-- mode_obj_pair contains mode object data for a particular mode_header record, which it 
--   points at via the mode_header_id field.  This table stores information only about pairs
--   of mode objects.  Each mode_obj_pair record points at two mode_obj_single records, one
--   corresponding to the observed object (via mode_obj_obs) and one corresponding to the 
--   forecast object (via mode_obj_fcst). 

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
);



--  look-up table data

INSERT INTO data_file_lu VALUES (0, 'point_stat', 'Verification statistics for forecasts at observation points');
INSERT INTO data_file_lu VALUES (1, 'grid_stat', 'Verification statistics for a matched forecast and observation grid');
INSERT INTO data_file_lu VALUES (2, 'mode_cts', 'Contingency table counts and statistics comparing forecast and observations');
INSERT INTO data_file_lu VALUES (3, 'mode_obj', 'Attributes for simple objects, merged cluster objects and pairs of objects');
INSERT INTO data_file_lu VALUES (4, 'wavelet_stat', 'Verification statistics for intensity-scale comparison of forecast and observations');

INSERT INTO line_type_lu VALUES (0, 'FHO', 'Forecast, Hit, Observation line type');
INSERT INTO line_type_lu VALUES (1, 'CTC', 'Contingency Table Counts line type');
INSERT INTO line_type_lu VALUES (2, 'CTS', 'Contingency Table Statistics line type');
INSERT INTO line_type_lu VALUES (3, 'CNT', 'Continuous statistics line type');
INSERT INTO line_type_lu VALUES (4, 'PCT', 'Probability contingency table count line type');
INSERT INTO line_type_lu VALUES (5, 'PSTD', 'Probabilistic statistics for dichotomous outcome line type');
INSERT INTO line_type_lu VALUES (6, 'PJC', 'Probabilistic Joint/Continuous line type');
INSERT INTO line_type_lu VALUES (7, 'PRC', 'Probability ROC points line type');
INSERT INTO line_type_lu VALUES (8, 'SL1L2', 'Scalar L1L2 line type');
INSERT INTO line_type_lu VALUES (9, 'SAL1L2', 'Scalar Anomaly L1L2 line type');
INSERT INTO line_type_lu VALUES (10, 'VL1L2', 'Vector L1L2 line type');
INSERT INTO line_type_lu VALUES (11, 'VAL1L2', 'Vector Anomaly L1L2 line type');
INSERT INTO line_type_lu VALUES (12, 'MPR', 'Matched Pair line type');
INSERT INTO line_type_lu VALUES (13, 'NBRCTC', 'Neighborhood Contingency Table Counts line type');
INSERT INTO line_type_lu VALUES (14, 'NBRCTS', 'Neighborhood Contingency Table Statistics line type');
INSERT INTO line_type_lu VALUES (15, 'NBRCNT', 'Neighborhood Continuous statistics line type');
INSERT INTO line_type_lu VALUES (16, 'ISC', 'Intensity Scale line type for the wavelet-stat tool');
INSERT INTO line_type_lu VALUES (17, 'MODE_OBJ_SINGLE', 'Mode object line containing data for a single object');
INSERT INTO line_type_lu VALUES (18, 'MODE_OBJ_PAIR', 'Mode object line containing data for a pair of objects');
INSERT INTO line_type_lu VALUES (19, 'MODE_CTS', 'Mode CTS line containing contingency table data');

INSERT INTO stat_group_lu VALUES(0, 'BASER', 'Base rate including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(1, 'FMEAN', 'Forecast mean including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(2, 'ACC', 'Accuracy including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(3, 'FBIAS', 'Frequency Bias including bootstrap upper and lower confidence limits', FALSE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(4, 'PODY', 'Probability of detecting yes including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(5, 'PODN', 'Probability of detecting no including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(6, 'POFD', 'Probability of FALSE detection including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(7, 'FAR', 'FALSE alarm ratio including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(8, 'CSI', 'Critical Success Index including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(9, 'GSS', 'Gilbert Skill Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(10, 'HK', 'Hanssen-Kuipers Discriminant including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(11, 'HSS', 'Heidke Skill Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(12, 'ODDS', 'Odds Ratio including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 2);
INSERT INTO stat_group_lu VALUES(13, 'FBAR', 'Forecast mean including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(14, 'FSTDEV', 'Standard deviation of the forecasts including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(15, 'OBAR', 'Observation mean including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(16, 'OSTDEV', 'Standard deviation of the observations including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(17, 'PR_CORR', 'Pearson correlation coefficient including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(18, 'ME', 'Mean error (F-O) including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(19, 'ESTDEV', 'Standard deviation of the error including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(20, 'MBIAS', 'Multiplicative bias including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(21, 'MAE', 'Mean absolute error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(22, 'MSE', 'Mean squared error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(23, 'BCMSE', 'Bias-corrected mean squared error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(24, 'RMSE', 'Root mean squared error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(25, 'E10', '10th percentile of the error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(26, 'E25', '25th percentile of the error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(27, 'E50', '50th percentile of the error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(28, 'E75', '75th percentile of the error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(29, 'E90', '90th percentile of the error including bootstrap upper and lower confidence limits', FALSE, TRUE, 3);
INSERT INTO stat_group_lu VALUES(30, 'BRIER', 'Brier Score including normal upper and lower confidence limits', TRUE, FALSE, 5);
INSERT INTO stat_group_lu VALUES(31, 'RELIABILITY', 'Reliability', FALSE, FALSE, 5);
INSERT INTO stat_group_lu VALUES(32, 'RESOLUTION', 'Resolution', FALSE, FALSE, 5);
INSERT INTO stat_group_lu VALUES(33, 'UNCERTAINTY', 'Uncertainty', FALSE, FALSE, 5);
INSERT INTO stat_group_lu VALUES(34, 'ROC_AUC', 'Area under the receiver operating characteristic curve', FALSE, FALSE, 5);
INSERT INTO stat_group_lu VALUES(35, 'NBR_BASER', 'Base rate including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(36, 'NBR_FMEAN', 'Forecast mean including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(37, 'NBR_ACC', 'Accuracy including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(38, 'NBR_FBIAS', 'Frequency Bias including bootstrap upper and lower confidence limits', FALSE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(39, 'NBR_PODY', 'Probability of detecting yes including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(40, 'NBR_PODN', 'Probability of detecting no including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(41, 'NBR_POFD', 'Probability of FALSE detection including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(42, 'NBR_FAR', 'FALSE alarm ratio including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(43, 'NBR_CSI', 'Critical Success Index including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(44, 'NBR_GSS', 'Gilbert Skill Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(45, 'NBR_HK', 'Hanssen-Kuipers Discriminant including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(46, 'NBR_HSS', 'Heidke Skill Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(47, 'NBR_ODDS', 'Odds Ratio including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 14);
INSERT INTO stat_group_lu VALUES(48, 'NBR_FBS', 'Fractions Brier Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 15);
INSERT INTO stat_group_lu VALUES(49, 'NBR_FSS', 'Fractions Skill Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 15);


-- metvdb_rev contains information about metvdb revisions, and provides an indicator of
--   the changes made in the current revision

CREATE TABLE metvdb_rev
(
    rev_id              INT UNSIGNED NOT NULL,
    rev_date            DATETIME,
    rev_name            VARCHAR(16),
    rev_detail          VARCHAR(2048),
    PRIMARY KEY (rev_id)    
);

INSERT INTO metvdb_rev VALUES (0, '2010-07-29 12:00:00', '0.1', 'Initial revision, includes metvdb_rev, instance_info and web_plot tables');
INSERT INTO metvdb_rev VALUES (0, '2010-10-14 12:00:00', '0.1', 'Increased web_plot.plot_xml field width to 65536');


-- instance_info contains information about the paricular instance of metvdb, including 
--   dates of data updates and information about data table contents

CREATE TABLE instance_info
(
    instance_info_id    INT UNSIGNED NOT NULL,
    updater             VARCHAR(64),
    update_date         DATETIME,
    update_detail       VARCHAR(2048),
    PRIMARY KEY (instance_info_id)    
);

INSERT INTO instance_info VALUES (0, 'pgoldenb', '2010-07-29 12:00:00', 'Initial load of data for testing new functionality');


-- web_plot contains information about plots made by the web application, including the
--   plot spec xml

CREATE TABLE web_plot
(
    web_plot_id         INT UNSIGNED NOT NULL,
    creation_date       DATETIME,
    plot_xml            VARCHAR(65536),
    PRIMARY KEY (web_plot_id)    
);


-- stat_fcst_var_stat maintains a join between fcst_var and stat names which is used
--   by the web app interface

CREATE
ALGORITHM = TEMPTABLE
VIEW stat_fcst_var_stat AS
SELECT DISTINCT
  sh.fcst_var,
  sgl.stat_group_lu_id,
  sgl.stat_group_name
FROM
  stat_header sh,
  stat_group sg,
  stat_group_lu sgl
WHERE
  sg.stat_header_id = sh.stat_header_id
  AND sg.stat_group_lu_id = sgl.stat_group_lu_id;
