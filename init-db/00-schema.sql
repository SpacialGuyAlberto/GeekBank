--
-- PostgreSQL database dump
--

-- Dumped from database version 16.2
-- Dumped by pg_dump version 16.2

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: customer; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA customer;


ALTER SCHEMA customer OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: customer; Type: TABLE; Schema: customer; Owner: postgres
--

CREATE TABLE customer.customer (
);


ALTER TABLE customer.customer OWNER TO postgres;

--
-- Name: account; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.account (
    balance double precision NOT NULL,
    id bigint NOT NULL,
    account_number character varying(255),
    user_id bigint NOT NULL,
    account_type character varying(255) NOT NULL,
    created_date timestamp(6) without time zone NOT NULL,
    currency character varying(255),
    daily_limit double precision NOT NULL,
    loyalty_points integer NOT NULL,
    status character varying(255) NOT NULL,
    updated_date timestamp(6) without time zone NOT NULL,
    verification_status character varying(255) NOT NULL,
    CONSTRAINT account_account_type_check CHECK (((account_type)::text = ANY ((ARRAY['SAVINGS'::character varying, 'CHECKING'::character varying, 'CREDIT'::character varying])::text[]))),
    CONSTRAINT account_status_check CHECK (((status)::text = ANY ((ARRAY['ACTIVE'::character varying, 'SUSPENDED'::character varying, 'CLOSED'::character varying, 'INACTIVE'::character varying])::text[]))),
    CONSTRAINT account_verification_status_check CHECK (((verification_status)::text = ANY ((ARRAY['VERIFIED'::character varying, 'UNVERIFIED'::character varying, 'PENDING'::character varying, 'REJECTED'::character varying])::text[])))
);


ALTER TABLE public.account OWNER TO postgres;

--
-- Name: account_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.account_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.account_id_seq OWNER TO postgres;

--
-- Name: account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.account_id_seq OWNED BY public.account.id;


--
-- Name: audit_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.audit_log (
    id bigint NOT NULL,
    action character varying(255),
    details character varying(255),
    "timestamp" timestamp(6) without time zone,
    user_id bigint
);


ALTER TABLE public.audit_log OWNER TO postgres;

--
-- Name: audit_log_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.audit_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.audit_log_id_seq OWNER TO postgres;

--
-- Name: audit_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.audit_log_id_seq OWNED BY public.audit_log.id;


--
-- Name: card; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.card (
    expiration date,
    id bigint NOT NULL,
    card_number character varying(255),
    card_type character varying(255),
    security_code character varying(255)
);


ALTER TABLE public.card OWNER TO postgres;

--
-- Name: cart_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cart_item (
    id bigint NOT NULL,
    product_id bigint,
    quantity integer NOT NULL,
    user_id bigint,
    price double precision NOT NULL
);


ALTER TABLE public.cart_item OWNER TO postgres;

--
-- Name: cart_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cart_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cart_item_id_seq OWNER TO postgres;

--
-- Name: cart_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cart_item_id_seq OWNED BY public.cart_item.id;


--
-- Name: customer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.customer (
    id integer NOT NULL,
    email character varying(255),
    first_name character varying(255),
    last_name character varying(255),
    password character varying(255)
);


ALTER TABLE public.customer OWNER TO postgres;

--
-- Name: customer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.customer_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.customer_id_seq OWNER TO postgres;

--
-- Name: customer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.customer_id_seq OWNED BY public.customer.id;


--
-- Name: feedbacks; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.feedbacks (
    id bigint NOT NULL,
    created_at timestamp(6) without time zone,
    gift_card_id bigint,
    message character varying(255),
    score integer NOT NULL,
    user_id bigint
);


ALTER TABLE public.feedbacks OWNER TO postgres;

--
-- Name: feedbacks_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.feedbacks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.feedbacks_id_seq OWNER TO postgres;

--
-- Name: feedbacks_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.feedbacks_id_seq OWNED BY public.feedbacks.id;


--
-- Name: gift_cards; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.gift_cards (
    kinguin_id bigint NOT NULL,
    activation_details character varying(1000),
    age_rating character varying(100),
    description character varying(1000),
    name character varying(500),
    offers_count integer NOT NULL,
    original_name character varying(500),
    platform character varying(255),
    price double precision NOT NULL,
    product_id character varying(500),
    qty integer NOT NULL,
    region_id integer NOT NULL,
    regional_limitations character varying(500),
    release_date character varying(255),
    text_qty integer NOT NULL,
    total_qty integer NOT NULL
);


ALTER TABLE public.gift_cards OWNER TO postgres;

--
-- Name: highlight_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.highlight_item (
    id bigint NOT NULL,
    product_id bigint
);


ALTER TABLE public.highlight_item OWNER TO postgres;

--
-- Name: highlight_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.highlight_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.highlight_item_id_seq OWNER TO postgres;

--
-- Name: highlight_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.highlight_item_id_seq OWNED BY public.highlight_item.id;


--
-- Name: password_reset_token; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.password_reset_token (
    id bigint NOT NULL,
    expiry_date timestamp(6) without time zone,
    token character varying(255),
    user_id bigint NOT NULL
);


ALTER TABLE public.password_reset_token OWNER TO postgres;

--
-- Name: password_reset_token_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.password_reset_token_seq
    START WITH 1
    INCREMENT BY 50
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.password_reset_token_seq OWNER TO postgres;

--
-- Name: product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.product (
    id bigint NOT NULL,
    description character varying(255),
    name character varying(255),
    price double precision NOT NULL,
    product_id character varying(255) NOT NULL
);


ALTER TABLE public.product OWNER TO postgres;

--
-- Name: transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transaction_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transaction_id_seq OWNER TO postgres;

--
-- Name: transaction; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transaction (
    amount double precision NOT NULL,
    id bigint DEFAULT nextval('public.transaction_id_seq'::regclass) NOT NULL,
    transaction_number character varying(255),
    description character varying(255),
    status character varying(255) NOT NULL,
    "timestamp" timestamp(6) without time zone NOT NULL,
    type character varying(255) NOT NULL,
    user_id bigint,
    account_id bigint,
    phone_number character varying(255) NOT NULL,
    expires_at timestamp without time zone DEFAULT now() NOT NULL,
    CONSTRAINT transaction_status_check CHECK (((status)::text = ANY (ARRAY['NOT_ACTIVE'::text, 'ACTIVE'::text, 'COMMITTED'::text, 'ROLLING_BACK'::text, 'MARKED_ROLLBACK'::text, 'FAILED_COMMIT'::text, 'FAILED_ROLLBACK'::text, 'COMMITTING'::text, 'ROLLED_BACK'::text, 'PENDING'::text, 'COMPLETED'::text, 'CANCELLED'::text, 'FAILED'::text]))),
    CONSTRAINT transaction_type_check CHECK (((type)::text = ANY (ARRAY['DEPOSIT'::text, 'WITHDRAWAL'::text, 'TRANSFER'::text, 'REFUND'::text, 'PURCHASE'::text, 'BALANCE_PURCHASE'::text])))
);


ALTER TABLE public.transaction OWNER TO postgres;

--
-- Name: transaction_product; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transaction_product (
    id bigint NOT NULL,
    quantity integer NOT NULL,
    product_id bigint NOT NULL,
    transaction_id bigint NOT NULL
);


ALTER TABLE public.transaction_product OWNER TO postgres;

--
-- Name: transaction_product_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transaction_product_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transaction_product_id_seq OWNER TO postgres;

--
-- Name: transaction_product_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transaction_product_id_seq OWNED BY public.transaction_product.id;


--
-- Name: transaction_products; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.transaction_products (
    id bigint NOT NULL,
    product_id bigint NOT NULL,
    quantity integer NOT NULL,
    transaction_id bigint NOT NULL
);


ALTER TABLE public.transaction_products OWNER TO postgres;

--
-- Name: transaction_products_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.transaction_products_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.transaction_products_id_seq OWNER TO postgres;

--
-- Name: transaction_products_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.transaction_products_id_seq OWNED BY public.transaction_products.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    is_enabled boolean NOT NULL,
    id bigint NOT NULL,
    email character varying(255) NOT NULL,
    name character varying(255) NOT NULL,
    password character varying(255),
    role character varying(255) NOT NULL,
    activation_token character varying(255),
    phone_number character varying(255),
    account_id bigint,
    new_email character varying(255),
    temp_name character varying(255),
    temp_phone_number character varying(255)
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.users_id_seq OWNED BY public.users.id;


--
-- Name: wished_item; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.wished_item (
    id bigint NOT NULL,
    price double precision NOT NULL,
    product_id bigint,
    quantity integer NOT NULL,
    user_id bigint
);


ALTER TABLE public.wished_item OWNER TO postgres;

--
-- Name: wished_item_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.wished_item_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.wished_item_id_seq OWNER TO postgres;

--
-- Name: wished_item_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.wished_item_id_seq OWNED BY public.wished_item.id;


--
-- Name: account id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.account ALTER COLUMN id SET DEFAULT nextval('public.account_id_seq'::regclass);


--
-- Name: audit_log id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log ALTER COLUMN id SET DEFAULT nextval('public.audit_log_id_seq'::regclass);


--
-- Name: cart_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cart_item ALTER COLUMN id SET DEFAULT nextval('public.cart_item_id_seq'::regclass);


--
-- Name: customer id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer ALTER COLUMN id SET DEFAULT nextval('public.customer_id_seq'::regclass);


--
-- Name: feedbacks id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks ALTER COLUMN id SET DEFAULT nextval('public.feedbacks_id_seq'::regclass);


--
-- Name: highlight_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.highlight_item ALTER COLUMN id SET DEFAULT nextval('public.highlight_item_id_seq'::regclass);


--
-- Name: transaction_product id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_product ALTER COLUMN id SET DEFAULT nextval('public.transaction_product_id_seq'::regclass);


--
-- Name: transaction_products id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_products ALTER COLUMN id SET DEFAULT nextval('public.transaction_products_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users ALTER COLUMN id SET DEFAULT nextval('public.users_id_seq'::regclass);


--
-- Name: wished_item id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wished_item ALTER COLUMN id SET DEFAULT nextval('public.wished_item_id_seq'::regclass);


--
-- Name: account account_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT account_pkey PRIMARY KEY (id);


--
-- Name: audit_log audit_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.audit_log
    ADD CONSTRAINT audit_log_pkey PRIMARY KEY (id);


--
-- Name: card card_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.card
    ADD CONSTRAINT card_pkey PRIMARY KEY (id);


--
-- Name: cart_item cart_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cart_item
    ADD CONSTRAINT cart_item_pkey PRIMARY KEY (id);


--
-- Name: customer customer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.customer
    ADD CONSTRAINT customer_pkey PRIMARY KEY (id);


--
-- Name: feedbacks feedbacks_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.feedbacks
    ADD CONSTRAINT feedbacks_pkey PRIMARY KEY (id);


--
-- Name: gift_cards gift_cards_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.gift_cards
    ADD CONSTRAINT gift_cards_pkey PRIMARY KEY (kinguin_id);


--
-- Name: highlight_item highlight_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.highlight_item
    ADD CONSTRAINT highlight_item_pkey PRIMARY KEY (id);


--
-- Name: password_reset_token password_reset_token_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT password_reset_token_pkey PRIMARY KEY (id);


--
-- Name: product product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.product
    ADD CONSTRAINT product_pkey PRIMARY KEY (id);


--
-- Name: transaction transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction
    ADD CONSTRAINT transaction_pkey PRIMARY KEY (id);


--
-- Name: transaction_product transaction_product_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_product
    ADD CONSTRAINT transaction_product_pkey PRIMARY KEY (id);


--
-- Name: transaction_products transaction_products_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_products
    ADD CONSTRAINT transaction_products_pkey PRIMARY KEY (id);


--
-- Name: users uk_1yov8c5ew74vlt8qra6cewq99; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_1yov8c5ew74vlt8qra6cewq99 UNIQUE (account_id);


--
-- Name: password_reset_token uk_f90ivichjaokvmovxpnlm5nin; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT uk_f90ivichjaokvmovxpnlm5nin UNIQUE (user_id);


--
-- Name: users uk_sx468g52bpetvlad2j9y0lptc; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT uk_sx468g52bpetvlad2j9y0lptc UNIQUE (email);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: wished_item wished_item_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wished_item
    ADD CONSTRAINT wished_item_pkey PRIMARY KEY (id);


--
-- Name: users fk3pwaj86pwopu3ot96qlrfo2up; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT fk3pwaj86pwopu3ot96qlrfo2up FOREIGN KEY (account_id) REFERENCES public.account(id);


--
-- Name: transaction_products fk3x18hiv3x9rw1tfhxq8d7pnuu; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_products
    ADD CONSTRAINT fk3x18hiv3x9rw1tfhxq8d7pnuu FOREIGN KEY (transaction_id) REFERENCES public.transaction(id);


--
-- Name: transaction fk6g20fcr3bhr6bihgy24rq1r1b; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction
    ADD CONSTRAINT fk6g20fcr3bhr6bihgy24rq1r1b FOREIGN KEY (account_id) REFERENCES public.account(id);


--
-- Name: password_reset_token fk83nsrttkwkb6ym0anu051mtxn; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.password_reset_token
    ADD CONSTRAINT fk83nsrttkwkb6ym0anu051mtxn FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: transaction fkanjpo5tiapru7an6cw4cu37y4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction
    ADD CONSTRAINT fkanjpo5tiapru7an6cw4cu37y4 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: transaction_product fkg4pbq1m3u41i2cbobs8dffe31; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_product
    ADD CONSTRAINT fkg4pbq1m3u41i2cbobs8dffe31 FOREIGN KEY (transaction_id) REFERENCES public.transaction(id);


--
-- Name: cart_item fkka3t831w0aw2vrwgsbhcn5y4m; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cart_item
    ADD CONSTRAINT fkka3t831w0aw2vrwgsbhcn5y4m FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: wished_item fklu4g7upr5ng5u84vhwwbonp7d; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.wished_item
    ADD CONSTRAINT fklu4g7upr5ng5u84vhwwbonp7d FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- Name: transaction_product fkoauxdr0f04miixeui3mn1yq3n; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.transaction_product
    ADD CONSTRAINT fkoauxdr0f04miixeui3mn1yq3n FOREIGN KEY (product_id) REFERENCES public.gift_cards(kinguin_id);


--
-- Name: account fkra7xoi9wtlcq07tmoxxe5jrh4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.account
    ADD CONSTRAINT fkra7xoi9wtlcq07tmoxxe5jrh4 FOREIGN KEY (user_id) REFERENCES public.users(id);


--
-- PostgreSQL database dump complete
--

