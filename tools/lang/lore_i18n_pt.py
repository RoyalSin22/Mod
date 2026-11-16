"""Textos narrativos del Grimorio en portugués (pt_br). Traducción del inglés de lore_catalog.py.

Glosario (coherente con el resto del mod): Essência, guildas, Véu, Fogueira, Concórdia, Conclave, Limiar, Primeira Era,
Eclipse, Ossuário Quebrado, Andarilho Velado, Autômato Antigo, Custódio, Escorificador de Ferro, Superfície (Overworld).
Cada entrada conserva los mismos párrafos, en el mismo orden, que el texto inglés. Las criaturas no llevan título.
"""

TEXTS = {}


def entry(entry_id, title, body):
    TEXTS[entry_id] = (title, body.strip())


entry('lore.cosmology.essence_within', 'A Essência que Habita em Todos', """
Antes de as guildas erguerem suas fortalezas, antes de os mestres registrarem seus ensinamentos, os mortais já possuíam o que mais tarde seria chamado de Essência.

Não era um dom dos deuses, nem uma força reservada a poucos escolhidos.

Habitava naturalmente os seres vivos.

Alguns aprenderam a fortalecer o corpo. Outros descobriram habilidades que pareciam superar os limites de sua natureza. Muitos nunca compreenderam o que despertava essas possibilidades.

Os primeiros mestres observaram que a prática constante transformava as capacidades de seus aprendizes.

Assim começou o estudo das Aptitudes.

As guildas não criaram esse poder.

Aprenderam a desenvolvê-lo.
""")

entry('lore.guild.twenty_paths', 'Os Vinte Caminhos', """
Durante gerações, os mestres transmitiram suas técnicas sem pertencer a uma tradição comum.

Cada comunidade desenvolveu maneiras diferentes de lutar, sobreviver, construir e compreender o mundo.

Com o tempo, surgiram grandes escolas.

Vinte dessas tradições alcançaram uma influência extraordinária e foram reconhecidas pelas guildas da Primeira Era.

Seus ensinamentos não eram idênticos.

Alguns defendiam o domínio das armas; outros buscavam compreender as forças arcanas, o comportamento das criaturas ou as possibilidades do conhecimento.

Mas todos compartilhavam um princípio: o poder deve ser cultivado por meio da experiência.

Seus nomes sobreviveram mesmo depois que suas fortalezas desapareceram.
""")

entry('lore.doctrine.three_doctrines', 'As Três Doutrinas', """
Os antigos mestres descobriram que uma mesma disciplina podia levar a formas de maestria muito diferentes.

Quem compartilhava um treinamento inicial começou a se distinguir à medida que desenvolvia suas habilidades.

Alguns preferiam a resistência. Outros aperfeiçoavam a precisão, a adaptação ou métodos que seus companheiros mal compreendiam.

O Conselho das Três Vias, convocado pela Concórdia, reconheceu três grandes doutrinas para cada guilda.

Nenhuma foi declarada superior.

Cada uma exigia anos de dedicação e uma compreensão particular dos ensinamentos originais.

Escolher um caminho significava renunciar aos outros: a verdadeira maestria exige profundidade.
""")

entry('lore.chronology.council_three_ways', 'O Conselho das Três Vias', """
Nos dias de maior prosperidade da Primeira Era, os representantes das grandes guildas se reuniram para comparar seus métodos de ensino.

A diversidade de técnicas havia crescido tanto que até mestres da mesma disciplina discutiam sobre a maneira correta.

O Conselho não resolveu essas diferenças proclamando vencedores.

Reconheceu que uma tradição podia conter várias respostas igualmente legítimas.

As doutrinas foram ordenadas, seus ensinamentos registrados e suas técnicas transmitidas às novas gerações.

Durante muito tempo, esse acordo permitiu que as guildas compartilhassem conhecimento sem abrir mão de suas diferenças.

O nome do Conselho sobreviveu às instituições que o convocaram.
""")

entry('lore.faction.concordance', 'A Concórdia das Guildas', """
A Primeira Era conheceu um tempo em que as vinte tradições trabalhavam juntas sob uma organização comum.

Chamaram-na de Concórdia.

Seus representantes compartilhavam métodos de treinamento, ferramentas e descobertas. As rotas entre os assentamentos permitiam que os mestres viajassem e que os aprendizes aprendessem técnicas diferentes das de sua terra natal.

A Concórdia nunca conseguiu eliminar todas as rivalidades.

Tampouco tentou.

Seu propósito era impedir que as diferenças destruíssem o conhecimento que cada guilda havia acumulado.

Mas havia perguntas para as quais nenhum acordo bastava.

As mais perigosas começaram quando os pesquisadores decidiram estudar o que restava após a morte.
""")

entry('lore.cosmology.echoes', 'A Pegada dos Caídos', """
Os primeiros estudiosos da morte observaram que certas manifestações persistiam depois do último suspiro.

Não eram corpos vivos.

Tampouco podiam ser descritas com certeza como mentes completas.

Alguns pesquisadores as chamaram de Ecos.

Sua origem e sua persistência intrigavam os mestres da Primeira Era.

Uma parte das guildas sustentava que deviam apenas ser estudadas.

Outros viam nelas a oportunidade de compreender o que separava a vida da morte.

Essa diferença parecia insignificante.

Com o tempo, tornou-se uma das grandes discordâncias da civilização.
""")

entry('lore.cosmology.veil_frontier', 'A Fronteira Invisível', """
Os pesquisadores descobriram que as dimensões não eram completamente isoladas.

Havia fenômenos capazes de cruzar certas fronteiras, embora a maioria dos seres vivos nunca os percebesse.

Chamaram-nos, em conjunto, de Véu.

No início, acreditavam que todas as suas regiões obedeciam às mesmas leis.

Depois descobriram que algumas fronteiras ofereciam uma resistência extraordinária.

Não pareciam proteger uma cidade, uma tumba ou algum reino conhecido.

Simplesmente barravam o caminho.

Ninguém sabia o que havia por trás delas.

E, durante algum tempo, ninguém achou necessário descobrir.
""")

entry('lore.faction.permanence', 'Os Pesquisadores da Permanência', """
A mortalidade havia sido aceita por gerações como uma condição inevitável.

Mas as descobertas sobre a persistência da Essência levantaram novas perguntas.

Seria possível preservar uma consciência depois da morte?

Seria possível conservar o conhecimento de alguém mesmo quando seu corpo já não existisse?

Os pesquisadores do Conclave buscaram respostas.

Nem todos desejavam alcançar a imortalidade.

Alguns pretendiam compreender o destino da memória. Outros esperavam evitar que gerações inteiras de conhecimento se perdessem.

Quando detectaram sinais vindos de uma região inacessível do Véu, acreditaram ter encontrado um indício do que procuravam.

Ainda não sabiam o que esperava do outro lado.
""")

entry('lore.chronology.first_answer', 'A Primeira Resposta', """
Os instrumentos registraram um sinal que não souberam explicar.

Os pesquisadores repetiram o experimento.

O sinal voltou.

Começaram a formular perguntas e descobriram padrões que pareciam respondê-las.

Logo chegaram respostas que continham conhecimento desconhecido.

Alguns membros do Conclave afirmaram ter encontrado uma consciência capaz de sobreviver sem corpo.

Outros suspeitaram de que aquela presença nunca havia sido humana.

Os experimentos continuaram.

Nenhum documento sobrevivente indica que a entidade tenha exigido a construção de um mecanismo de comunicação.

Mas as respostas foram, aos poucos, guiando os pesquisadores até essa possibilidade.

E eles decidiram segui-las.
""")

entry('lore.artifact.sealed_artifact', 'O Artefato Selado', """
Os pesquisadores construíram um objeto capaz de reter um sinal e amplificá-lo por meio da Essência.

Seu propósito original era estabelecer uma comunicação mais intensa com a região desconhecida do Véu.

O experimento superou todas as expectativas.

Quando a conexão alcançou uma força sem precedentes, uma vontade alheia conseguiu exercer influência sobre o mundo.

O artefato sobreviveu ao desastre.

Os últimos guardiões da Primeira Era não ousaram destruí-lo.

Trancaram-no atrás de quatro câmaras e confiaram sua proteção a máquinas que continuariam obedecendo mesmo depois que seus criadores se fossem.
""")

entry('lore.event.first_eclipse', 'A Noite da Primeira Queda', """
Os relatos antigos descrevem um céu escurecido e criaturas que deixaram de obedecer aos próprios instintos.

Os caminhos tornaram-se inseguros.

As defesas construídas para deter invasões comuns mostraram-se insuficientes.

As guildas mobilizaram seus mestres e combatentes.

Os pesquisadores tentaram fechar a conexão que haviam aberto.

O fenômeno terminou antes que o que restava por trás do Véu conseguisse atravessá-lo.

Mas a Primeira Era nunca recuperou por completo seu antigo esplendor.

As disputas se intensificaram, as rotas foram abandonadas e os arquivos se dispersaram.

Durante gerações, os sobreviventes recordariam aquele acontecimento por um único nome:

O Primeiro Eclipse.
""")

entry('lore.temple.last_mandate', 'O Último Mandato', """
O santuário não foi construído para conceder recompensas a quem se mostrasse forte o bastante.

Foi preparado para impedir que o conhecimento mais perigoso da Primeira Era voltasse a ser usado.

Os mecanismos de acesso foram protegidos.

Um Autômato recebeu a ordem de vigiar os corredores.

O Custódio permaneceu ao lado das câmaras interiores, equipado com as quatro chaves que controlavam suas fechaduras.

Nenhuma máquina devia interpretar as intenções dos visitantes.

Devia obedecer.

Os últimos guardiões humanos desapareceram, mas suas ordens continuaram sendo cumpridas.
""")

entry('lore.event.current_eclipse', 'O Olhar do Prisioneiro', """
A chama consumiu a relíquia e um sinal atravessou o Véu.

O mundo pareceu parar por um instante.

Então veio a escuridão.

As criaturas começaram a se comportar de maneiras extraordinárias. Algumas perseguiam aventureiros com uma persistência inédita; outras pareciam superar obstáculos que antes as teriam detido.

Não havia nenhum exército descendo de outro plano.

A presença se serviu do que já habitava o mundo.

Pela primeira vez desde os acontecimentos da Primeira Era, a vontade aprisionada encontrara outra oportunidade de estender sua influência.
""")

entry('lore.event.silent_fracture', 'A Fratura Silenciosa', """
O céu recuperou sua aparência.

Os perseguidores encerraram a caçada e as Fogueiras arderam como antes.

Os sobreviventes acreditaram que a ameaça havia desaparecido.

Mas o confinamento não havia voltado ao seu estado original.

Em uma região do Véu, persistia uma alteração que não existia antes do acontecimento.

A prisão continuava fechada.

Seu ocupante continuava isolado.

Mas uma das fronteiras que resistiam desde a Grande Separação havia sido enfraquecida.

O Eclipse havia terminado.

Suas consequências, não.
""")

entry('lore.arc2.unfinished_oath', 'O Juramento Inacabado', """
Os Guardiões dos Selos sustentavam que o confinamento devia ser preservado.

Os Discípulos do Limiar defendiam que nenhuma prisão podia ser compreendida apenas pelo medo que seu ocupante inspirava.

Os primeiros haviam presenciado as consequências do contato.

Os segundos haviam encontrado indícios de que o confinamento nascera da separação natural dos mundos.

Ambas as tradições guardavam verdades importantes.

Nenhuma conhecia a história inteira.

A Fratura ainda existia.

Não havia método comprovado para repará-la, mas tampouco motivo suficiente para abri-la.

Os últimos documentos deixaram um aviso:

Antes de decidir o destino do que permanece confinado, será preciso compreender por que o universo o separou do resto de seus mundos.
""")

entry('lore.bestiary.errante_velado', None, """
Nem todos os mortos retornam em sua antiga forma.

Alguns parecem arrastar fragmentos do que foram, como se sua existência tivesse ficado presa entre dois estados.

O Andarilho Velado carrega uma presença inquietante, oculta sob os restos de vestes decompostas e um véu espectral.

Os estudiosos da Primeira Era teriam reconhecido nele sinais de uma manifestação incompleta da Essência.

Ainda não sabemos se ele guarda lembranças de sua vida anterior.

Só sabemos que algo permanece.
""")

entry('lore.bestiary.osario_quebrado', None, """
Os antigos pesquisadores observaram que certos restos podiam conservar manifestações extraordinárias mesmo depois de desaparecido todo sinal reconhecível de vida.

O Ossuário Quebrado representa um desses fenômenos.

Sua existência levanta uma pergunta que intrigava os mestres da Primeira Era:

Pode a Essência manter unidos restos materiais sem uma consciência que os governe?

Os registros não oferecem uma resposta definitiva.
""")

entry('lore.bestiary.ancient_automaton', None, """
Os engenheiros da Primeira Era construíram máquinas capazes de cumprir ordens muito depois de seus operadores abandonarem as instalações.

O Autômato Antigo é um testemunho desse conhecimento.

Ele não luta por ódio nem por ambição.

Permanece onde seus construtores o deixaram, cumprindo uma função cuja importância talvez tenha se perdido ao longo dos séculos.

Sua derrota não significa necessariamente o fim de uma ameaça maligna.

Pode significar que uma antiga defesa deixou de funcionar.
""")

entry('lore.bestiary.custodio_cuatro_llaves', None, """
O Custódio foi concebido para proteger os mecanismos de um santuário excepcional.

As quatro chaves ligadas à sua estrutura controlavam o acesso às câmaras interiores.

Ele não julga as intenções de quem se aproxima.

Não distingue um ladrão de um aventureiro que busca respostas.

Sua função é barrar o caminho.

Durante gerações, cumpriu essa ordem.

Quem conseguir derrotá-lo descobrirá que algumas portas foram fechadas por razões muito diferentes de guardar um tesouro.
""")

entry('lore.bestiary.escorificador_hierro', None, """
O Escorificador de Ferro pertence às ameaças que acompanham as incursões vindas do Nether e às que habitam seus territórios.

Sua presença na Superfície lembra que as fronteiras dimensionais podem ser alteradas por conexões extraordinárias.

Nem toda fratura do Véu leva aos lugares que nenhum pesquisador jamais alcançou.

Algumas abrem caminhos para regiões que os aventureiros já conhecem, embora seus habitantes não pertençam ao mundo que atravessaram.
""")

entry('lore.event.soul_fog_chronicle', 'Névoa das Almas', """
Quando a neblina cobre os caminhos, os aldeões recordam antigos relatos de criaturas que retornam da morte.

Alguns afirmam que o fenômeno começou após a queda da Primeira Era.

Outros insistem que ele já existia antes das guildas.

Nenhum testemunho explicou plenamente sua origem.

Os pesquisadores encontraram indícios de manifestações relacionadas aos Ecos da Essência, mas seus documentos ficaram incompletos.

A névoa não anuncia necessariamente a chegada do que as guildas temiam.

Pode representar uma alteração diferente das fronteiras que separam a matéria de certas manifestações residuais.
""")

entry('lore.event.ember_rain_chronicle', 'Chuva de Brasas', """
Os portais permitem cruzar distâncias que as leis comuns do mundo manteriam separadas.

Mas algumas conexões podem perturbar temporariamente o equilíbrio entre as dimensões.

A Chuva de Brasas é uma manifestação desse perigo.

Criaturas do Nether irrompem na Superfície, trazendo ameaças para as quais muitos assentamentos não estão preparados.

O fenômeno recorda um ensinamento esquecido dos pesquisadores do Véu:

Abrir um caminho entre dois mundos não significa controlar o que pode percorrê-lo.
""")

entry('lore.event.eclipse_chronicle', 'Eclipse', """
Durante o Primeiro Eclipse, os mestres da Primeira Era descobriram que uma vontade externa podia exercer influência sobre as criaturas do mundo.

Muito tempo depois, o sacrifício de uma oferenda permite que essa conexão volte a se estabelecer.

Os campeões e elites afetados adquirem uma extraordinária capacidade de procurar seus alvos.

O fenômeno não é a libertação do Prisioneiro.

É uma manifestação temporária de sua vontade.

E, enquanto os aventureiros lutam para sobreviver, algo permanece oculto por trás das fronteiras do Véu.
""")
