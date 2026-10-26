-- Existing invalid durations must be reviewed explicitly; do not silently truncate clinical agenda.
ALTER TABLE consulta ADD CONSTRAINT consulta_duracao_maxima CHECK (fim - inicio <= interval '8 hours');
CREATE INDEX usuario_clinica_nome ON usuario(clinica_id,nome,id);
CREATE INDEX vinculo_usuario_clinica ON vinculo(usuario_id,clinica_id,paciente_id);
