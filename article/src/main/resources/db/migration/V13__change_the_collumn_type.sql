ALTER TABLE monograph_chapter
ALTER COLUMN mein_points
    TYPE double precision
    USING mein_points::double precision;