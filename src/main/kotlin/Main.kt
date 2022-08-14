import kotlin.system.exitProcess

const val AMOUNT_OF_INITIAL_CARDS = 4
const val AMOUNT_OF_FULL_PLAYER_DECK = 6

class CardDeck {
    private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K").reversed()
    private val suits = listOf("♣", "♦", "♥", "♠")
    val cards = mutableListOf<String>()

    fun reset() {
        cards.clear()
    }

    fun shuffle() {
        cards.shuffle()
    }

    //getting cards from deck
    fun get(playerCardsList: MutableList<String>, amountOfCards: Int) {
        var i = 0
        while (i < amountOfCards){
            playerCardsList.add(cards[i])
            i++
        }
        cards.removeAll(playerCardsList)
    }

    init {
        for (i in suits) {
            for (j in ranks) {
                cards.add(j+i)
            }
        }
    }
}

open class Player {
    val playerDeck = mutableListOf<String>()
    val playerWonCardsDeck = mutableListOf<String>()
    var playingCard: String = ""
    open val name = "Player"
    var score = 0
    var isPlayFirst = false
    var isLastWinner = false

    open fun showDeck() {
        print("Cards in hand: ")
        var numForNumeration = 1
        for (i in playerDeck) {
            print("$numForNumeration)$i ")
            numForNumeration += 1
        }
        println()
    }

    open fun playCard(tableDeck: CardDeck) {
        while (true) {
            println("Choose a card to play (1-${playerDeck.size}):")
            val numberOfCard = readln()
            try {
                if (playerDeck.size == 1) {
                    playingCard = playerDeck[0]
                    playerDeck.remove(playingCard)
                    break
                } else {
                    if (numberOfCard.toInt() in 1 .. playerDeck.size) {
                        playingCard = playerDeck[numberOfCard.toInt() - 1]
                        playerDeck.remove(playingCard)
                        break
                    } else {
                        continue
                    }
                }
            } catch (e: NumberFormatException) {
                if (numberOfCard == "exit") {
                    println("Game Over")
                    exitProcess(0)
                } else {
                    continue
                }
            }
        }
        tableDeck.cards.add(playingCard) //adding played card to table deck
    }

    open fun updatePoints(cardsFromTable: MutableList<String>) {
        val pointsCards = mutableListOf("A", "10", "J", "Q", "K")
        for (i in cardsFromTable) {
            for (j in pointsCards) {
                if (i.contains(j)) {
                    score += 1
                }
            }
        }
    }
}

class AIPlayer : Player() {
    override val name = "Computer"
    override fun playCard(tableDeck: CardDeck) {
        val candidateCards = mutableListOf(mutableListOf(), mutableListOf<String>())
        if (playerDeck.size == 1) {
            playingCard = playerDeck[0]
        } else {
            if (tableDeck.cards.isEmpty()) {
                lookForAlikeCards()
            } else {
                checkDeckForCandidateCards(tableDeck, candidateCards)

                if (candidateCards[0].isEmpty() && candidateCards[1].isEmpty()) {
                    lookForAlikeCards()
                } else if (candidateCards[0].size == 1 && candidateCards[1].isEmpty()) {
                    playingCard = candidateCards[0][0]
                } else if (candidateCards[1].size == 1 && candidateCards[0].isEmpty()) {
                    playingCard = candidateCards[1][0]
                } else {
                    playingCard = if (candidateCards[0].size > 0 && candidateCards[1].size < candidateCards[0].size) {
                        candidateCards[0][(0 until candidateCards[0].size).random()]
                    } else if (candidateCards[1].size > candidateCards[0].size && candidateCards[1].size > 0) {
                        candidateCards[1][(0 until candidateCards[1].size).random()]
                    } else {
                        candidateCards[(0..1).random()][0]
                    }
                }
            }
        }
        playerDeck.remove(playingCard)

        tableDeck.cards.add(playingCard)

        println("Computer plays $playingCard")
        println()
    }

    override fun showDeck() {
        printList(playerDeck)
    }

    private fun checkDeckForCandidateCards(tableDeck: CardDeck, candidateCards: MutableList<MutableList<String>>) {
        val lastCardOnTable = tableDeck.cards.last()
        for (i in playerDeck) {
            if (i.last() == lastCardOnTable.last()) { //suit
                candidateCards[0].add(i)
            } else if (i.substring(0, i.lastIndex) == lastCardOnTable.substring(0, lastCardOnTable.lastIndex)) { //rank
                candidateCards[1].add(i)
            }
        }
    }

    private fun lookForAlikeCards() {
        val alikeCardsWithSuits = mutableListOf<String>()
        val alikeCardsWithRanks = mutableListOf<String>()
        for (i in 0 until playerDeck.size) {
            for (j in i + 1 until playerDeck.size) {
                if (playerDeck[i].last() == playerDeck[j].last()) {
                    alikeCardsWithSuits.add(playerDeck[i])
                } else if (playerDeck[i].substring(0, playerDeck[i].lastIndex) == playerDeck[j].substring(0, playerDeck[j].lastIndex)) {
                    alikeCardsWithRanks.add(playerDeck[i])
                }
            }
        }
        playingCard = if (alikeCardsWithSuits.isEmpty() && alikeCardsWithRanks.isEmpty()) {
            val numberOfCard = (0 until playerDeck.size).random()
            playerDeck[numberOfCard]
        } else if (alikeCardsWithSuits.isNotEmpty()){
            alikeCardsWithSuits[0]
        } else {
            alikeCardsWithRanks[0]
        }
    }
}

fun main() {
    val cardDeck = CardDeck()
    cardDeck.shuffle()
    val tableDeck = CardDeck()
    tableDeck.reset()
    cardDeck.get(tableDeck.cards, AMOUNT_OF_INITIAL_CARDS)

    val user = Player()
    val computerPlayer = AIPlayer()
    println("Indigo Card Game")

    while (true) {
        println("Play first?")
        val whoPlaysFirst = readln()

        if (whoPlaysFirst == "yes") {
            user.isPlayFirst = true
            game(cardDeck, tableDeck, player1 = user, player2 = computerPlayer)
            break
        } else if (whoPlaysFirst == "no") {
            computerPlayer.isPlayFirst = true
            game(cardDeck, tableDeck, player1 = computerPlayer, player2 = user) //change value of arguments, so the first player to move is AI
            break
        } else {
            continue
        }
    }
}

fun game(cardDeck: CardDeck, tableDeck: CardDeck, player1: Player, player2: Player) {
    print("Initial cards on the table: ")
    printList(tableDeck.cards)
    println()

    for (i in 1..28) {
        if (player1.playerDeck.size > 0) {
            moveOfPlayer(tableDeck, player1, playerForStats = player2)
            moveOfPlayer(tableDeck, player2, playerForStats = player1)
        } else if (i == 23) {
            break
        } else {
            reloadDecksOfPlayers(cardDeck, player1, player2)
        }
    }
    if (tableDeck.cards.isNotEmpty()) {
        println("${tableDeck.cards.size} cards on the table, and the top card is ${tableDeck.cards.last()}")
    } else {
        println("No cards on the table")
    }

    if (player1.isLastWinner && tableDeck.cards.isNotEmpty()) {
        player1.updatePoints(tableDeck.cards)
        tableDeck.get(player1.playerWonCardsDeck, tableDeck.cards.size)
    } else if (player2.isLastWinner && tableDeck.cards.isNotEmpty()){
        player2.updatePoints(tableDeck.cards)
        tableDeck.get(player2.playerWonCardsDeck, tableDeck.cards.size)
    }

    if (player1.playerWonCardsDeck.size > player2.playerWonCardsDeck.size) {
        player1.score += 3
    } else if (player1.playerWonCardsDeck.size == player2.playerWonCardsDeck.size) {
        if (player1.isPlayFirst) {
            player1.score += 3
        } else {
            player2.score += 3
        }
    } else {
        player2.score += 3
    }

    if (player1 !is AIPlayer) {
        showScore(player1, player2)
    } else {
        showScore(player2, player1)
    }

    println("Game Over")
}

fun reloadDecksOfPlayers(cardDeck: CardDeck, user: Player, computerPlayer: Player) {
    cardDeck.get(user.playerDeck, AMOUNT_OF_FULL_PLAYER_DECK)
    cardDeck.get(computerPlayer.playerDeck, AMOUNT_OF_FULL_PLAYER_DECK)
}

fun moveOfPlayer(tableDeck: CardDeck, player: Player, playerForStats: Player) {
    if (tableDeck.cards.isNotEmpty()) {
        println("${tableDeck.cards.size} cards on the table, and the top card is ${tableDeck.cards.last()}")
    } else {
        println("No cards on the table")
    }
    player.showDeck()

    player.playCard(tableDeck)
    if (tableDeck.cards.size > 1) {
        val lastTableDeckCard = tableDeck.cards[tableDeck.cards.lastIndex - 1]
        val playerCard = player.playingCard
        if (playerCard.last() == lastTableDeckCard.last() || playerCard.substring(0, playerCard.lastIndex) == lastTableDeckCard.substring(0, lastTableDeckCard.lastIndex)) {
            player.updatePoints(tableDeck.cards)
            tableDeck.get(player.playerWonCardsDeck, tableDeck.cards.size)

            println("${player.name} wins cards")

            player.isLastWinner = true
            playerForStats.isLastWinner = false

            if (player !is AIPlayer) {
                showScore(player, playerForStats)
            } else {
                showScore(playerForStats, player)
            }
        }
    }
}

fun showScore(player1: Player, player2: Player) {
    println("Score: Player ${player1.score} - Computer ${player2.score}")
    println("Cards: Player ${player1.playerWonCardsDeck.size} - Computer ${player2.playerWonCardsDeck.size}")
    println()
}

//function for collection output
fun printList(list: List<String>) {
    for (i in list){
        print("$i ")
    }
    println()
}