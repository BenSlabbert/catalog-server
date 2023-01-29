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
	VALUES('seeded item');
