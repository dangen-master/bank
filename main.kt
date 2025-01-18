import java.time.LocalDate
import kotlin.math.roundToInt

// Класс для представления банков
data class Bank(
    val name: String, // Полное наименование банка
    val shortName: String, // Краткое наименование банка
    val interestRate: Double // Процент на остаток на текущем счете (от 0.1% до 2%)
)

// Класс для представления физических лиц
data class Person(
    val fullName: String, // ФИО
    val inn: String, // ИНН
    val passportNumber: String, // Номер паспорта
    val passportSeries: String, // Серия паспорта
    var accounts: MutableList<BankAccount> = mutableListOf(), // Список счетов
    var credits: MutableList<Credit> = mutableListOf(), // Список кредитов
    var deposits: MutableList<DepositAccount> = mutableListOf() // Список депозитов
)

// Класс для представления банковских счетов
sealed class BankAccount(
    val bank: Bank,
    var balance: Double, // Остаток на счете
    val accountType: AccountType
) {
    // Перевод средств между счетами
    fun transferTo(account: BankAccount, amount: Double) {
        if (balance >= amount) {
            balance -= amount
            account.balance += amount
            println("Перевод успешно выполнен: $amount на счет ${account.accountType} в банк ${account.bank.shortName}")
        } else {
            println("Недостаточно средств для перевода.")
        }
    }

    enum class AccountType {
        CURRENT, CREDIT, DEPOSIT
    }
}

// Класс для представления текущего счета
class CurrentAccount(bank: Bank, balance: Double) : BankAccount(bank, balance, BankAccount.AccountType.CURRENT)

// Класс для представления кредитного счета
class CreditAccount(bank: Bank, balance: Double, var loanAmount: Double) : BankAccount(bank, balance, BankAccount.AccountType.CREDIT)

// Класс для представления депозитного счета
class DepositAccount(
    bank: Bank,
    balance: Double,
    val interestRate: Double, // Процент по депозиту
    val depositPeriod: Int, // Период депозита в месяцах
    val isWithdrawable: Boolean, // Можно ли снимать средства
    val isRenewable: Boolean // Пролонгируемый ли депозит
) : BankAccount(bank, balance, BankAccount.AccountType.DEPOSIT) {
    // Метод для расчета предполагаемой суммы дохода
    fun calculateIncome(): Double {
        return balance * (interestRate / 100) * depositPeriod
    }
}

// Класс для представления кредита
class Credit(
    val bank: Bank,
    val loanAmount: Double, // Сумма кредита
    val interestRate: Double, // Процент по кредиту
    val loanTerm: Int, // Срок кредита в месяцах
    val startDate: LocalDate, // Дата начала кредита
    var paidAmount: Double = 0.0 // Сумма погашения
) {
    // Метод для расчета суммы переплаты по кредиту
    fun calculateOverpayment(): Double {
        val totalPayment = loanAmount * (1 + interestRate / 100 * loanTerm)
        return totalPayment - loanAmount
    }

    // Метод для получения графика платежей по кредиту
    fun getPaymentSchedule(): List<Pair<LocalDate, Double>> {
        val monthlyPayment = loanAmount / loanTerm + (loanAmount * interestRate / 100) / loanTerm
        val schedule = mutableListOf<Pair<LocalDate, Double>>()
        var currentDate = startDate
        for (i in 1..loanTerm) {
            schedule.add(Pair(currentDate, monthlyPayment))
            currentDate = currentDate.plusMonths(1)
        }
        return schedule
    }

    // Метод для частичного погашения кредита
    fun partialRepayment(amount: Double) {
        if (amount <= loanAmount - paidAmount) {
            paidAmount += amount
            println("Частичное погашение выполнено на сумму: $amount")
        } else {
            println("Сумма погашения больше остатка по кредиту.")
        }
    }

    // Метод для полного погашения кредита
    fun fullRepayment() {
        paidAmount = loanAmount
        println("Кредит полностью погашен.")
    }
}

// Функция для создания банка
fun createBank(name: String, shortName: String, interestRate: Double): Bank {
    return Bank(name, shortName, interestRate)
}

// Функция для создания физического лица
fun createPerson(fullName: String, inn: String, passportNumber: String, passportSeries: String): Person {
    return Person(fullName, inn, passportNumber, passportSeries)
}

// Пример использования приложения
fun main() {
    // Создаем банки
    val bank1 = createBank("Сбербанк", "Сбер", 1.5)
    val bank2 = createBank("Тинькофф", "Тинькофф", 2.0)

    // Создаем физическое лицо
    val person = createPerson("Иванов Иван Иванович", "123456789012", "1234 567890", "12 34 567890")

    // Создаем текущие счета
    val currentAccount1 = CurrentAccount(bank1, 1000.0)
    val currentAccount2 = CurrentAccount(bank1, 500.0)
    val currentAccount3 = CurrentAccount(bank2, 2000.0)

    // Добавляем счета физическому лицу
    person.accounts.add(currentAccount1)
    person.accounts.add(currentAccount2)
    person.accounts.add(currentAccount3)

    // Переводим средства между счетами
    currentAccount1.transferTo(currentAccount2, 200.0)

    // Создаем депозитный счет
    val depositAccount = DepositAccount(bank1, 10000.0, 18.0, 12, true, true)
    person.deposits.add(depositAccount)

    // Расчет дохода по депозиту
    println("Предполагаемый доход по депозиту: ${depositAccount.calculateIncome()}")

    // Создаем кредит
    val credit = Credit(bank2, 50000.0, 15.0, 12, LocalDate.now())
    person.credits.add(credit)

    // Получаем график платежей по кредиту
    println("График погашения кредита: ${credit.getPaymentSchedule()}")

    // Частичное погашение кредита
    credit.partialRepayment(5000.0)
    credit.partialRepayment(20000.0)

    // Полное погашение кредита
    credit.fullRepayment()
}
