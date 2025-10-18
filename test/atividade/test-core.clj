(ns atividade.core-test
  (:require [clojure.test :refer [deftest is testing]]
            [atividade.core :as core]))

(deftest test-criar-tarefa
  (testing "Deve criar uma tarefa com nome e descrição"
    (let [tarefa (core/criar-tarefa "Estudar Clojure" "Aprender TDD")]
      (is (= "Estudar Clojure" (:name tarefa)))
      (is (= "Aprender TDD" (:description tarefa)))
      (is (= :em-andamento (:status tarefa)))
      (is (some? (:id tarefa))))))  

(deftest test-rejeitar-tarefa-sem-nome
  (testing "Não pode tarefa sem nome"
    (is (thrown? IllegalArgumentException
            (core/criar-tarefa "" "Descrição")))))

(deftest test-marcar-como-concluida
  (testing "Deve marcar uma tarefa como concluída"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)]
      (is (= :concluida (:status tarefa-concluida))))))

(deftest test-nao-remarcar-concluida
  (testing "Não deve remarcar tarefa já concluída"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)
          tarefa-remarcada (core/marcar-concluida tarefa-concluida)]
      (is (= :concluida (:status tarefa-remarcada)))
      (is (= tarefa-concluida tarefa-remarcada)))))

(deftest test-marcar-em-andamento
  (testing "Deve marcar uma tarefa como em andamento"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-andamento (core/marcar-em-andamento tarefa)]
      (is (= :em-andamento (:status tarefa))))))

(deftest test-nao-voltar-concluida-para-andamento
  (testing "Não deve marcar como em andamento uma tarefa concluída"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)]
      (is (thrown? IllegalStateException
                   (core/marcar-em-andamento tarefa-concluida))))))

(deftest test-editar-tarefa
  (testing "Deve permitir editar nome e descrição"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-editada (core/editar-tarefa tarefa "Aprender" "TDD com Clojure")]
      (is (= "Aprender" (:name tarefa-editada)))
      (is (= "TDD com Clojure" (:description tarefa-editada)))
      (is (= (:id tarefa) (:id tarefa-editada))) 
      (is (= (:status tarefa) (:status tarefa-editada))))))

(deftest test-nao-editar-com-nome-vazio
  (testing "Não deve permitir editar com nome vazio"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")]
      (is (thrown? IllegalArgumentException
                   (core/editar-tarefa tarefa "" "Nova descrição"))))))

(deftest test-excluir-tarefa
  (testing "Deve remover uma tarefa da lista"
    (let [tarefas [{:id 1 :name "Estudar" :description "Clojure" :status :em-andamento}
                   {:id 2 :name "Exercitar" :description "Corrida" :status :em-andamento}]
          tarefas-atualizadas (core/excluir-tarefa tarefas 1)]
      (is (= 1 (count tarefas-atualizadas)))
      (is (not-any? #(= 1 (:id %)) tarefas-atualizadas)))))

(deftest test-excluir-tarefa-inexistente
  (testing "Deve lançar erro ao tentar excluir tarefa inexistente"
    (let [tarefas [{:id 1 :name "Estudar" :description "Clojure" :status :em-andamento}
                   {:id 2 :name "Exercitar" :description "Corrida" :status :em-andamento}]]
      (is (thrown? IllegalArgumentException
                   (core/excluir-tarefa tarefas 999))))))