(ns atividade.core
  (:require [clojure.string :as str]))

(defn- validar-nome! [nome]
  (when (str/blank? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio"))))

(defn criar-tarefa
  [nome descricao]
  (validar-nome! nome)
  {:id 1
   :name nome
   :description descricao
   :status :em-andamento})

(defn marcar-concluida
  [tarefa]
  (if (= :concluida (:status tarefa))
    tarefa
    (assoc tarefa :status :concluida)))

(defn marcar-em-andamento
  [tarefa]
  (when (= :concluida (:status tarefa))
    (throw (IllegalStateException. "Tarefa concluída não pode voltar para em andamento")))
  (if (= :em-andamento (:status tarefa))
    tarefa
    (assoc tarefa :status :em-andamento)))

(defn editar-tarefa
  [tarefa nome descricao]
  (validar-nome! nome)
  (if (and (= nome (:name tarefa))
           (= descricao (:description tarefa)))
    tarefa
    (assoc tarefa
      :name nome
      :description descricao)))

(defn excluir-tarefa
  [tarefas id]
  (let [[acc-t encontrou?]
        (reduce (fn [[acc ok?] t]
                  (if (= id (:id t))
                    [acc true]
                    [(conj! acc t) ok?]))
                [(transient []) false]
                tarefas)]
    (when-not encontrou?
      (throw (IllegalArgumentException. "Tarefa não encontrada")))
    (persistent! acc-t)))
    