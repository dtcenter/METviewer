
-- increase the width of the thresh fields to accomodate threshold lists
ALTER TABLE stat_header MODIFY COLUMN fcst_thresh VARCHAR(128);
ALTER TABLE stat_header MODIFY COLUMN obs_thresh VARCHAR(128);

-- fix a schema problem in line_data_mpr
ALTER TABLE line_data_mpr MODIFY COLUMN mp_index INT UNSIGNED;
ALTER TABLE line_data_mpr ADD COLUMN obs_sid VARCHAR(32) AFTER mp_index;


-- line_data_mctc contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mctc;
CREATE TABLE line_data_mctc
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    n_cat               INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_mctc_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


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
    CONSTRAINT line_data_mctc_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_mctc(line_data_id)
);


-- line_data_mcts contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_mcts;
CREATE TABLE line_data_mcts
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    alpha               DOUBLE,
    n_cat               INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_mcts_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_rhist contains stat data for a particular stat_header record, which it points 
--   at via the stat_header_id field.

DROP TABLE IF EXISTS line_data_rhist;
CREATE TABLE line_data_rhist
(
    line_data_id        INT UNSIGNED NOT NULL,
    stat_header_id      INT UNSIGNED NOT NULL,    
    data_file_id        INT UNSIGNED NOT NULL,
    line_num            INT UNSIGNED,
    total               INT UNSIGNED,
    n_rank              INT UNSIGNED,
    PRIMARY KEY (line_data_id),
    CONSTRAINT line_data_rhist_stat_header_id_pk
            FOREIGN KEY(stat_header_id)
            REFERENCES stat_header(stat_header_id)
);


-- line_data_rhist_rank contains rank data for a particular line_data_rhist record.  The 
--   number of ranks stored is given by the line_data_rhist field n_rank.

DROP TABLE IF EXISTS line_data_rhist_rank;
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

DROP TABLE IF EXISTS line_data_orank;
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

DROP TABLE IF EXISTS line_data_orank_ens;
CREATE TABLE line_data_orank_ens
(
    line_data_id        INT UNSIGNED NOT NULL,
    i_value             INT UNSIGNED NOT NULL,
    ens_i               DOUBLE,
    CONSTRAINT line_data_orank_id_pk
            FOREIGN KEY(line_data_id)
            REFERENCES line_data_orank(line_data_id)
);

INSERT INTO data_file_lu VALUES (5, 'ensemble_stat', 'Ensemble verification statistics');

INSERT INTO line_type_lu VALUES (20, 'MCTC', 'Multi-category Contingency Table Counts line type');
INSERT INTO line_type_lu VALUES (21, 'MCTS', 'Multi-category Contingency Table Statistics line type');
INSERT INTO line_type_lu VALUES (22, 'RHIST', 'Ensemble Rank Histogram line type');
INSERT INTO line_type_lu VALUES (23, 'ORANK', 'Ensemble Observation Rank line type');

INSERT INTO stat_group_lu VALUES(50, 'PSTD_BASER', 'The Base Rate, including normal upper and lower confidence limits', TRUE, FALSE, 5);
INSERT INTO stat_group_lu VALUES(51, 'MCTS_ACC', 'Accuracy including normal and bootstrap upper and lower confidence limits', TRUE, TRUE, 21);
INSERT INTO stat_group_lu VALUES(52, 'MCTS_HK', 'Hanssen-Kuipers Discriminant including normal and bootstrap upper and lower confidence limits', FALSE, TRUE, 21);
INSERT INTO stat_group_lu VALUES(53, 'MCTS_HSS', 'Heidke Skill Score including bootstrap upper and lower confidence limits', FALSE, TRUE, 21);
INSERT INTO stat_group_lu VALUES(54, 'MCTS_GER', 'Gerrity Score and bootstrap confidence limits', FALSE, TRUE, 21);
INSERT INTO stat_group_lu VALUES(55, 'CRPS', 'Continuous Ranked Probability Score', FALSE, FALSE, 22);
INSERT INTO stat_group_lu VALUES(56, 'IGN', 'Ignorance score', FALSE, FALSE, 22);

-- ALTER TABLE metvdb_rev ADD COLUMN rev_date DATETIME AFTER rev_id;
INSERT INTO metvdb_rev VALUES (1, '2010-10-14 12:00:00', '0.1', 'Increased web_plot.plot_xml field to MEDIUMTEXT');
INSERT INTO metvdb_rev VALUES (2, '2010-11-15 12:00:00', '0.3', 'METViewer changes to support out from METv3.0');
