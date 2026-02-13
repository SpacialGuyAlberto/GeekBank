-- Fix schema for Users table
CREATE TABLE IF NOT EXISTS public.users (
    id bigint NOT NULL,
    activation_token character varying(255),
    affiliate_link character varying(255),
    commission_rate double precision,
    email character varying(255) NOT NULL,
    is_enabled boolean NOT NULL,
    name character varying(255) NOT NULL,
    password character varying(255),
    phone_number character varying(255),
    promo_code character varying(255),
    role character varying(255) NOT NULL,
    game_player_id character varying(255),
    game_player_name character varying(255),
    team_name character varying(255),
    dtype character varying(255),
    account_id bigint,
    new_email character varying(255),
    temp_name character varying(255),
    temp_phone_number character varying(255),
    CONSTRAINT users_pkey PRIMARY KEY (id)
);

ALTER TABLE public.users OWNER TO postgres;

-- Add columns if table already exists (redundant if CREATE above works, but safe)
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS affiliate_link VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS commission_rate DOUBLE PRECISION;
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS promo_code VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS game_player_id VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS game_player_name VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS team_name VARCHAR(255);
ALTER TABLE public.users ADD COLUMN IF NOT EXISTS dtype VARCHAR(255);
