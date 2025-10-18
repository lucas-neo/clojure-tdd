# To‑Do List — TDD em Clojure (passo a passo por testes)

> Relato de desenvolvimento **dirigido por testes** seguindo o ciclo **Red → Green → Refactor**, organizado exatamente por **TEST‑01…TEST‑10**.

---

## Estrutura do projeto

Para iniciar um projeto com testes, definimos a estrutura básica:

```
root/
├── deps.edn
├── src/
│   └── atividade/
│       └── core.clj
└── test/
    └── atividade/
        └── core_test.clj
```

* `src/` e `test/` são os *paths* de código e testes.
* Namespaces: `atividade.core` (código) e `atividade.core-test` (testes).

### Como executar os testes

```bash
clj -M:test -e "(require 'atividade.core-test) (clojure.test/run-tests 'atividade.core-test)"
```

---

## TEST‑01 — Criar tarefa (REQ‑01)

**Objetivo:** criar tarefa com **nome**, **descrição**, `:status :em-andamento` e `:id` presente.

### RED

```clj
;; test/atividade/core_test.clj
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
```

**Saída (falha esperada):**

```zsh
Testing atividade.core-test

FAIL in (test-criar-tarefa) (core_test.clj:8)
expected: (= "Estudar Clojure" (:name tarefa))
  actual: (not (= "Estudar Clojure" nil))
...
Ran 1 tests containing 4 assertions.
4 failures, 0 errors.
```

### GREEN (mínimo para passar)

```clj
;; src/atividade/core.clj
(ns atividade.core)

(defn criar-tarefa [nome descricao]
  {:id 1
   :name nome
   :description descricao
   :status :em-andamento})
```

**Saída (verde):**

```zsh
Ran 1 tests containing 4 assertions.
0 failures, 0 errors.
```

---

## TEST‑02 — Validar nome obrigatório (REQ‑01)

**Objetivo:** rejeitar criação de tarefa **sem nome**.

### RED

```clj
(deftest test-rejeitar-tarefa-sem-nome
  (testing "Deve rejeitar criação de tarefa com nome vazio"
    (is (thrown? IllegalArgumentException
                 (core/criar-tarefa "" "Descrição")))))
```

**Saída (falha esperada):**

```zsh
FAIL in (test-rejeitar-tarefa-sem-nome) (core_test.clj:15)
expected: (thrown? IllegalArgumentException ...)
  actual: nil
```

### GREEN

```clj
(defn criar-tarefa [nome descricao]
  (when (empty? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio")))
  {:id 1 :name nome :description descricao :status :em-andamento})
```

**Saída (verde):**

```zsh
Ran 2 tests containing 5 assertions.
0 failures, 0 errors.
```

---

## TEST‑03 — Marcar como concluída (REQ‑02)

**Objetivo:** atualizar `:status` para `:concluida`.

### RED

```clj
(deftest test-marcar-como-concluida
  (testing "Deve marcar uma tarefa como concluída"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)]
      (is (= :concluida (:status tarefa-concluida))))))

(defn marcar-concluida [tarefa]
  nil)
```

**Saída (falha esperada):**

```zsh
FAIL in (test-marcar-como-concluida) (core_test.clj:22)
expected: (= :concluida (:status tarefa-concluida))
  actual: (not (= :concluida nil))
```

### GREEN

```clj
(defn marcar-concluida [tarefa]
  (assoc tarefa :status :concluida))
```

**Saída (verde):**

```zsh
Ran 3 tests containing 6 assertions.
0 failures, 0 errors.
```

---

## TEST‑04 — Idempotência ao concluir (REQ‑02)

**Objetivo:** remarcar concluída **não altera**.

### Teste

```clj
(deftest test-nao-remarcar-concluida
  (testing "Não deve remarcar tarefa já concluída"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)
          tarefa-remarcada (core/marcar-concluida tarefa-concluida)]
      (is (= :concluida (:status tarefa-remarcada)))
      (is (= tarefa-concluida tarefa-remarcada)))))
```

**Saída (verde):**

```zsh
Ran 4 tests containing 8 assertions.
0 failures, 0 errors.
```

> Nota: já passava com a implementação do TEST‑03 porque `assoc` em mapa já resultava no mesmo mapa quando o valor era igual.

---

## TEST‑05 — Marcar como em andamento (REQ‑03)

**Objetivo:** garantir `:status :em-andamento`.

### RED

```clj
(deftest test-marcar-em-andamento
  (testing "Deve marcar uma tarefa como em andamento"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)
          tarefa-andamento (core/marcar-em-andamento tarefa-concluida)]
      (is (= :em-andamento (:status tarefa-andamento))))))

(defn marcar-em-andamento [tarefa]
  nil)
```

**Saída (falha esperada):**

```zsh
FAIL in (test-marcar-em-andamento) (core_test.clj:37)
expected: (= :em-andamento (:status tarefa-andamento))
  actual: (not (= :em-andamento nil))
```

### GREEN (inicial)

```clj
(defn marcar-em-andamento [tarefa]
  (assoc tarefa :status :em-andamento))
```

---

## TEST‑06 — Proibir voltar concluída → andamento (REQ‑03)

**Objetivo:** lançar `IllegalStateException` se tarefa estiver `:concluida`.

### RED

```clj
(deftest test-nao-voltar-concluida-para-andamento
  (testing "Não deve marcar como em andamento uma tarefa concluída"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-concluida (core/marcar-concluida tarefa)]
      (is (thrown? IllegalStateException
                   (core/marcar-em-andamento tarefa-concluida))))))
```

**Saída (falha esperada):**

```zsh
FAIL in (test-nao-voltar-concluida-para-andamento) (core_test.clj:43)
expected: (thrown? IllegalStateException ...)
  actual: nil
```

### GREEN (final + ajuste no TEST‑05)

```clj
(defn marcar-em-andamento [tarefa]
  (when (= :concluida (:status tarefa))
    (throw (IllegalStateException. "Tarefa concluída não pode voltar para em andamento")))
  (assoc tarefa :status :em-andamento))
```

```clj
(deftest test-marcar-em-andamento
  (testing "Deve manter tarefa em andamento"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-andamento (core/marcar-em-andamento tarefa)]
      (is (= :em-andamento (:status tarefa-andamento))))))
```

**Saída (verde):**

```zsh
Ran 6 tests containing 10 assertions.
0 failures, 0 errors.
```

---

## TEST‑07 — Editar nome e descrição (REQ‑04)

**Objetivo:** atualizar `:name` e `:description` preservando `:id` e `:status`.

### RED

```clj
(deftest test-editar-tarefa
  (testing "Deve permitir editar nome e descrição"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")
          tarefa-editada (core/editar-tarefa tarefa "Aprender" "TDD com Clojure")]
      (is (= "Aprender" (:name tarefa-editada)))
      (is (= "TDD com Clojure" (:description tarefa-editada)))
      (is (= (:id tarefa) (:id tarefa-editada)))
      (is (= (:status tarefa) (:status tarefa-editada))))))

(defn editar-tarefa [tarefa nome descricao] nil)
```

**Saída (falha esperada):**

```zsh
FAIL ... 4 failures
```

### GREEN

```clj
(defn editar-tarefa [tarefa nome descricao]
  (assoc tarefa :name nome :description descricao))
```

**Saída (verde):**

```zsh
Ran 7 tests containing 14 assertions.
0 failures, 0 errors.
```

---

## TEST‑08 — Validar edição com nome vazio (REQ‑04)

**Objetivo:** lançar `IllegalArgumentException` quando `nome` vazio.

### RED

```clj
(deftest test-nao-editar-com-nome-vazio
  (testing "Não deve permitir editar com nome vazio"
    (let [tarefa (core/criar-tarefa "Estudar" "Clojure")]
      (is (thrown? IllegalArgumentException
                   (core/editar-tarefa tarefa "" "Nova descrição"))))))
```

**Saída (falha esperada):**

```zsh
FAIL ... expected: (thrown? IllegalArgumentException ...) actual: nil
```

### GREEN

```clj
(defn editar-tarefa [tarefa nome descricao]
  (when (empty? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio")))
  (assoc tarefa :name nome :description descricao))
```

**Saída (verde):**

```zsh
Ran 8 tests containing 15 assertions.
0 failures, 0 errors.
```

---

## TEST‑09 — Excluir tarefa existente (REQ‑05)

**Objetivo:** remover tarefa da lista por `:id`.

### RED

```clj
(deftest test-excluir-tarefa
  (testing "Deve remover uma tarefa da lista"
    (let [tarefas [{:id 1 :name "Estudar"  :description "Clojure" :status :em-andamento}
                   {:id 2 :name "Exercitar" :description "Corrida" :status :em-andamento}]
          tarefas-atualizadas (core/excluir-tarefa tarefas 1)]
      (is (= 1 (count tarefas-atualizadas)))
      (is (not-any? #(= 1 (:id %)) tarefas-atualizadas)))))

(defn excluir-tarefa [tarefas id] nil)
```

**Saída (falha esperada):**

```zsh
FAIL ... expected: (= 1 (count ...)) actual: (not (= 1 0))
```

### GREEN

```clj
(defn excluir-tarefa [tarefas id]
  (remove #(= id (:id %)) tarefas))
```

**Saída (verde):**

```zsh
Ran 9 tests containing 17 assertions.
0 failures, 0 errors.
```

---

## TEST‑10 — Excluir tarefa inexistente (REQ‑05)

**Objetivo:** lançar `IllegalArgumentException` ao tentar excluir `id` inexistente.

### RED

```clj
(deftest test-excluir-tarefa-inexistente
  (testing "Deve lançar erro ao tentar excluir tarefa inexistente"
    (let [tarefas [{:id 1 :name "Estudar"  :description "Clojure" :status :em-andamento}
                   {:id 2 :name "Exercitar" :description "Corrida"  :status :em-andamento}]]
      (is (thrown? IllegalArgumentException
                   (core/excluir-tarefa tarefas 999))))))
```

**Saída (falha esperada):**

```zsh
FAIL ... expected: (thrown? IllegalArgumentException ...) actual: nil
```

### GREEN

```clj
(defn excluir-tarefa [tarefas id]
  (when-not (some #(= id (:id %)) tarefas)
    (throw (IllegalArgumentException. "Tarefa não encontrada")))
  (remove #(= id (:id %)) tarefas))
```

**Saída (verde):**

```zsh
Ran 10 tests containing 18 assertions.
0 failures, 0 errors.
```

---

## Refactor — limpeza, idempotência e helpers

### Antes (trecho)

```clj
(ns atividade.core)

(defn criar-tarefa [nome descricao]
  (when (empty? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio")))
  {:id 1 :name nome :description descricao :status :em-andamento})

(defn marcar-concluida [tarefa]
  (assoc tarefa :status :concluida))

(defn marcar-em-andamento [tarefa]
  (when (= :concluida (:status tarefa))
    (throw (IllegalStateException. "Tarefa concluída não pode voltar para em andamento")))
  (assoc tarefa :status :em-andamento))

(defn editar-tarefa [tarefa nome descricao]
  (when (empty? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio")))
  (assoc tarefa :name nome :description descricao))

(defn excluir-tarefa [tarefas id]
  (when-not (some #(= id (:id %)) tarefas)
    (throw (IllegalArgumentException. "Tarefa não encontrada")))
  (remove #(= id (:id %)) tarefas))
```

### Depois (final)

```clj
(ns atividade.core
  (:require [clojure.string :as str]))

;; Helper centralizado
(defn- validar-nome! [nome]
  (when (str/blank? nome)
    (throw (IllegalArgumentException. "Nome não pode ser vazio"))))

(defn criar-tarefa [nome descricao]
  (validar-nome! nome)
  {:id 1 :name nome :description descricao :status :em-andamento})

(defn marcar-concluida [tarefa]
  ;; Idempotente
  (if (= :concluida (:status tarefa)) tarefa (assoc tarefa :status :concluida)))

(defn marcar-em-andamento [tarefa]
  (when (= :concluida (:status tarefa))
    (throw (IllegalStateException. "Tarefa concluída não pode voltar para em andamento")))
  ;; Idempotente
  (if (= :em-andamento (:status tarefa)) tarefa (assoc tarefa :status :em-andamento)))

(defn editar-tarefa [tarefa nome descricao]
  (validar-nome! nome)
  ;; Evita recriar objeto quando nada mudou
  (if (and (= nome (:name tarefa)) (= descricao (:description tarefa)))
    tarefa
    (assoc tarefa :name nome :description descricao)))

(defn excluir-tarefa [tarefas id]
  ;; Reduce com transient para eficiência
  (let [[acc-t encontrou?]
        (reduce (fn [[acc ok?] t]
                  (if (= id (:id t)) [acc true] [(conj! acc t) ok?]))
                [(transient []) false]
                tarefas)]
    (when-not encontrou? (throw (IllegalArgumentException. "Tarefa não encontrada")))
    (persistent! acc-t)))
```

---

## Considerações finais

* **Cobertura dos requisitos**: REQ‑01…REQ‑05 atendidos com testes positivos e negativos.
* **Contratos explícitos por exceção** para operações inválidas.
* **Idempotência** nas mudanças de status e na edição quando não há alteração.

### Próximos passos

* Geração real de `:id` (UUID/contador atômico).
* Repositório em memória/DB + `find-by-id`, `list`, `create!`, `update!`, `delete!`.
* Especificações com `clojure.spec`/Malli + property‑based testing (`test.check`).
* API HTTP (Ring) e testes de integração.
* CI no GitHub Actions.
