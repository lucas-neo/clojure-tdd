(ns atividade.core)

(defn criar-tarefa [nome descricao]
  (when (empty? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio")))
  {:id 1
   :name nome
   :description descricao
   :status :em-andamento})

(defn marcar-concluida [tarefa]
    (assoc tarefa :status :concluida))

(defn marcar-em-andamento [tarefa]
  (when (= :concluida (:status tarefa))
    (throw (IllegalStateException. "Tarefa concluída não pode voltar para em andamento")))
  (assoc tarefa :status :em-andamento))

(defn editar-tarefa [tarefa nome descricao]
  (when (empty? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio")))
  (assoc tarefa 
    :name nome
    :description descricao))

(defn excluir-tarefa [tarefas id]
  (when-not (some #(= id (:id %)) tarefas)
    (throw (IllegalArgumentException. "Tarefa não encontrada")))
  (remove #(= id (:id %)) tarefas))
  