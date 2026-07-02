# Usar skills

/prd-writer

• Você chama pelo nome na mensagem. Exemplos:

  Use a skill prd-writer para criar um PRD do produto X.

  ou:

  prd-writer: gere um PRD para [descrição do produto].
  Salve em docs/prd.md.

  O ideal é passar 3 coisas:

  Use prd-writer.

  PROJECT_NAME: Meu Produto
  OUTPUT_FOLDER: docs
  PRD_PATH: docs/prd-meu-produto.md
  PRODUCT_DESCRIPTION: [explique a ideia, problema, público, funcionalidades esperadas]

  Também funciona por intenção, sem nome explícito:

  Crie um PRD completo para uma plataforma que...

  Nesse caso eu devo detectar que a prd-writer se aplica, fazer perguntas de clarificação e depois gerar o PRD.




# Instalando Skills 

## Skill.sh java spring skill
```
npx skills add https://github.com/github/awesome-copilot --skill java-springboot
```
https://www.skills.sh/github/awesome-copilot/java-springboot

## Matt Popock Skills
```
npx skills@latest add mattpocock/skills
```
https://github.com/mattpocock/skills/tree/main  

# Chamando skills
/java-springboot 

Implemente a feature usando /java-springboot





# Criando especificação e plano

Usando:
```
spec-writer @docs/PRD.md F01
```
Gera spec da feature F01



# Desenvolvendo primeira feature e PR


Implementação da feature F01-xpto

Prompt para desenvolver usando skill instalada
```bash
Baseado no @docs/PRD.md e no @docs/F01-xpto.md, faça a implementação da usando a skill xpto.
```



