DROP TABLE cas_check_request;

CREATE TABLE public.cas_accommodation_assessment (
     id                         SERIAL NOT NULL constraint cas_accommodation_assessment_pk PRIMARY KEY,
     assessment_id 			    INTEGER NOT NULL,
     status 			        varchar(20) DEFAULT NULL,
     type               	    varchar(20) NULL,
     ineligibility_reason       varchar(200) NULL,
     areas_to_avoid_info 	    text NULL,
     supporting_info_for_referral 	text NULL,
     referred                   BOOLEAN NULL,
     address_id 	    	    INTEGER NULL,
     created_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     last_updated_timestamp TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP NOT NULL,
     CONSTRAINT status_chk CHECK (((status)::text = ANY ((
         ARRAY['PROPOSED'::character varying,
               'PERSON_INELIGIBLE'::character varying,
               'PERSON_ELIGIBLE'::character varying,
               'REFERRAL_REQUESTED'::character varying,
               'REFERRAL_ACCEPTED'::character varying,
               'REFERRAL_REFUSED'::character varying,
               'REFERRAL_WITHDRAWN'::character varying,
               'ADDRESS_PROVIDED'::character varying
         ])::text[]))),
     CONSTRAINT cas_typ_chk CHECK (((type)::text = ANY ((ARRAY['CAS_1'::character varying, 'CAS_2'::character varying])::text[]))),
     CONSTRAINT fk_accommodation_assessment_address FOREIGN KEY (address_id) REFERENCES public.address(id)
);

ALTER TABLE address ALTER COLUMN uprn DROP NOT NULL;
ALTER TABLE address ALTER COLUMN country DROP NOT NULL;
ALTER TABLE address ALTER COLUMN x_coordinate DROP NOT NULL;
ALTER TABLE address ALTER COLUMN y_coordinate DROP NOT NULL;