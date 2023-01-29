CREATE
	TABLE
		item(
			id serial,
			name text,
			replicated BOOLEAN DEFAULT FALSE,
			deleted BOOLEAN DEFAULT FALSE
		);

INSERT
	INTO
		item(name)
	VALUES('seeded item 1'),
	('seeded item 2'),
	('seeded item 3'),
	('seeded item 4'),
	('seeded item 5'),
	('seeded item 6');
