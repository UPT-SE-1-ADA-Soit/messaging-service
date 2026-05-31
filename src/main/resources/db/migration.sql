-- Run this once in the Supabase SQL editor before starting the messaging service.
-- Safe to run even if the table is empty.

-- 1. Rename columns to match the new schema
ALTER TABLE message RENAME COLUMN user1_id TO sender_id;
ALTER TABLE message RENAME COLUMN user2_id TO receiver_id;
ALTER TABLE message RENAME COLUMN message  TO content;

-- 2. Add timestamps
--    sent_at: existing rows receive the current timestamp as a best-effort default
ALTER TABLE message ADD COLUMN sent_at TIMESTAMPTZ NOT NULL DEFAULT NOW();
ALTER TABLE message ADD COLUMN read_at TIMESTAMPTZ;

-- 3. Index for fast conversation lookups
CREATE INDEX IF NOT EXISTS idx_message_conversation
    ON message (LEAST(sender_id, receiver_id), GREATEST(sender_id, receiver_id), sent_at);
