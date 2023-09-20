# LinkedIn Automation Bot using Java and Selenide

This repository contains the code for automating LinkedIn networking tasks such as adding new contacts. The project is built using Java and the Selenide library, and it's managed by Gradle.

## Table of Contents

- [Features](#features)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Contributing](#contributing)
- [License](#license)

## Features

- Automated login to LinkedIn
- Automated search for specific roles or keywords
- Automated sending of connection requests
- Error handling for rate limits and other issues

## Prerequisites

- Java JDK 8 or higher
- Gradle
- Microsoft Edge (or change the `browser` variable in `TestBase` class to use another browser)

## Installation

1. Clone the repository
    ```bash
    git clone https://github.com/oleksandrso/LinkedinConnectingPeople.git
    ```
2. Navigate to the project directory
    ```bash
    cd LinkedinConnectingPeople
    ```
3. Install the dependencies
    ```bash
    gradle build
    ```

## Usage

1. Update the `username` and `password` variables in the `Methods` class with your LinkedIn credentials.
2. Run the tests using Gradle with the following command, replacing `your-email@gmail.com` and `your-password` with your LinkedIn credentials:
    ```bash
    gradle test -DLogin=your-email@gmail.com -DPassword=your-password
    ```

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.
