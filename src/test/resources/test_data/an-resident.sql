insert into resident (  id,
                        forename,
                        surname,
                        phone_number,
                        relation,
                        date_of_birth,
                        age,
                        is_main_resident,
                        standard_address_check_request_id,
                        created_timestamp,
                        last_updated_timestamp)

values (1,
        'John',
        'Broo',
        '07867898923',
        'Father',
        '1978-03-20',
        30,
        true,
        1,
        '2020-03-31',
        '2020-03-31'),
(2,
    'Tom',
    'Kee',
    '07867898923',
    'Brother',
    '1978-03-20',
    30,
    false,
    1,
    '2020-03-30',
    '2020-03-30'),
       (3,
        'Josh',
        'Lee',
        '07867898923',
        'Mother',
        '1978-03-20',
        30,
        false,
        1,
        '2020-03-30',
        '2020-03-30');
