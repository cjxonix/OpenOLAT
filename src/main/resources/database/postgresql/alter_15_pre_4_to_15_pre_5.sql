-- grading
alter table o_grad_time_record add column g_metadata_time int8 default 0 not null;

-- CATALOG
UPDATE o_catentry SET short_title = name;